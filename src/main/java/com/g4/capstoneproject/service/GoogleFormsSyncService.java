package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.GoogleFormSyncRecord;
import com.g4.capstoneproject.entity.MedicalReport;
import com.g4.capstoneproject.entity.Survey;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.UserInfo;
import com.g4.capstoneproject.repository.GoogleFormSyncRecordRepository;
import com.g4.capstoneproject.repository.MedicalReportRepository;
import com.g4.capstoneproject.repository.SurveyRepository;
import com.g4.capstoneproject.repository.UserInfoRepository;
import com.g4.capstoneproject.repository.UserRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.FormsScopes;
import com.google.api.services.forms.v1.model.Answer;
import com.google.api.services.forms.v1.model.Form;
import com.google.api.services.forms.v1.model.FormResponse;
import com.google.api.services.forms.v1.model.Item;
import com.google.api.services.forms.v1.model.ListFormResponsesResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleFormsSyncService {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final GoogleFormSyncRecordRepository syncRecordRepository;
    private final SurveyRepository surveyRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${google.forms.ids:}")
    private String formIdsConfig;

    @Value("${google.forms.credentials.path:classpath:google-credentials.json}")
    private Resource credentialsResource;

    @Value("${google.forms.application-name:Capstone Project}")
    private String applicationName;

    @Value("${google.forms.default-password:}")
    private String defaultPassword;

    @Value("${google.forms.sync.max-responses-per-page:200}")
    private Integer maxResponsesPerPage;

    @Transactional
    public Map<String, Object> syncPatientsFromConfiguredForms(String triggerSource) {
        // Static IDs from configuration (for backward compatibility)
        List<String> formIds = new ArrayList<>(parseFormIds(formIdsConfig));
        // Dynamic IDs from Survey CRUD (bác sĩ chỉ cần dán link form vào survey)
        formIds.addAll(extractFormIdsFromSurveys());
        // Remove trùng
        formIds = formIds.stream().distinct().toList();

        if (formIds.isEmpty()) {
            throw new IllegalStateException("Chưa cấu hình form Google nào cho đồng bộ (google.forms.ids hoặc Survey.formUrl).");
        }

        SyncSummary summary = new SyncSummary(triggerSource, formIds.size());
        try {
            Forms formsService = createFormsService();
            for (String formId : formIds) {
                syncSingleForm(formsService, formId, summary);
            }
        } catch (Exception e) {
            log.error("Google Forms sync failed", e);
            throw new RuntimeException("Lỗi đồng bộ Google Forms: " + e.getMessage(), e);
        }
        return summary.toMap();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentSyncedPatients(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        List<GoogleFormSyncRecord> records = syncRecordRepository.findBySyncStatusAndPatientIsNotNullOrderBySyncedAtDesc(
                GoogleFormSyncRecord.SyncStatus.SYNCED,
                org.springframework.data.domain.PageRequest.of(0, safeLimit));

        List<Map<String, Object>> result = new ArrayList<>();
        for (GoogleFormSyncRecord record : records) {
            User patient = record.getPatient();
            if (patient == null) {
                continue;
            }
            MedicalReport medicalReport = record.getMedicalReport();
            Map<String, Object> row = new HashMap<>();
            row.put("syncRecordId", record.getId());
            row.put("patientId", patient.getId());
            row.put("fullName", patient.getUserInfo() != null ? patient.getUserInfo().getFullName() : null);
            row.put("phoneNumber", patient.getPhoneNumber());
            row.put("email", patient.getEmail());
            row.put("formId", record.getFormId());
            row.put("formTitle", record.getFormTitle());
            row.put("responseId", record.getResponseId());
            row.put("submittedAt", record.getSubmittedAt());
            row.put("medicalReportId", medicalReport != null ? medicalReport.getId() : null);
            row.put("medicalReportTitle", medicalReport != null ? medicalReport.getTitle() : null);
            row.put("callStatus", record.getCallStatus() != null
                    ? record.getCallStatus().name()
                    : GoogleFormSyncRecord.CallStatus.NOT_CALLED.name());
            row.put("calledAt", record.getCalledAt());
            result.add(row);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSurveyDetail(Long syncRecordId) {
        GoogleFormSyncRecord record = syncRecordRepository.findById(syncRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay ban ghi dong bo: " + syncRecordId));

        MedicalReport report = record.getMedicalReport();
        User patient = record.getPatient();

        Map<String, Object> detail = new HashMap<>();
        detail.put("syncRecordId", record.getId());
        detail.put("patientId", patient != null ? patient.getId() : null);
        detail.put("fullName", patient != null && patient.getUserInfo() != null ? patient.getUserInfo().getFullName() : null);
        detail.put("phoneNumber", patient != null ? patient.getPhoneNumber() : null);
        detail.put("email", patient != null ? patient.getEmail() : null);
        detail.put("formId", record.getFormId());
        detail.put("formTitle", record.getFormTitle());
        detail.put("responseId", record.getResponseId());
        detail.put("submittedAt", record.getSubmittedAt());
        detail.put("callStatus", record.getCallStatus() != null
                ? record.getCallStatus().name()
                : GoogleFormSyncRecord.CallStatus.NOT_CALLED.name());
        detail.put("calledAt", record.getCalledAt());
        detail.put("medicalReportId", report != null ? report.getId() : null);
        detail.put("medicalReportTitle", report != null ? report.getTitle() : null);
        detail.put("surveyContent", report != null ? report.getContent() : null);
        detail.put("surveyNotes", report != null ? report.getNotes() : null);
        return detail;
    }

    @Transactional
    public Map<String, Object> updateCallStatus(Long syncRecordId, GoogleFormSyncRecord.CallStatus callStatus) {
        GoogleFormSyncRecord record = syncRecordRepository.findById(syncRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay ban ghi dong bo: " + syncRecordId));
        record.setCallStatus(callStatus);
        record.setCalledAt(callStatus == GoogleFormSyncRecord.CallStatus.CALLED ? LocalDateTime.now(VIETNAM_ZONE) : null);
        syncRecordRepository.save(record);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("syncRecordId", record.getId());
        response.put("callStatus", record.getCallStatus().name());
        response.put("calledAt", record.getCalledAt());
        return response;
    }

    private void syncSingleForm(Forms formsService, String formId, SyncSummary summary) throws IOException {
        Form metadata = formsService.forms().get(formId).execute();
        String formTitle = metadata.getInfo() != null ? metadata.getInfo().getTitle() : formId;
        Map<String, String> questionMap = extractQuestionMap(metadata);

        String pageToken = null;
        do {
            var listRequest = formsService.forms().responses().list(formId)
                    .setPageSize(maxResponsesPerPage);
            if (pageToken != null) {
                listRequest.setPageToken(pageToken);
            }

            ListFormResponsesResponse response = listRequest.execute();
            List<FormResponse> responses = response.getResponses();
            if (responses != null) {
                for (FormResponse formResponse : responses) {
                    processResponse(formId, formTitle, questionMap, formResponse, summary);
                }
            }
            pageToken = response.getNextPageToken();
        } while (pageToken != null && !pageToken.isBlank());
    }

    private void processResponse(
            String formId,
            String formTitle,
            Map<String, String> questionMap,
            FormResponse formResponse,
            SyncSummary summary) {
        String responseId = formResponse.getResponseId();
        if (responseId == null || responseId.isBlank()) {
            summary.skippedInvalid++;
            return;
        }

        Optional<GoogleFormSyncRecord> existingRecordOpt = syncRecordRepository.findByFormIdAndResponseId(formId, responseId);
        if (existingRecordOpt.isPresent()
                && existingRecordOpt.get().getSyncStatus() == GoogleFormSyncRecord.SyncStatus.SYNCED) {
            summary.skippedDuplicate++;
            return;
        }

        GoogleFormSyncRecord record = existingRecordOpt.orElseGet(() -> GoogleFormSyncRecord.builder()
                .formId(formId)
                .responseId(responseId)
                .callStatus(GoogleFormSyncRecord.CallStatus.NOT_CALLED)
                .build());

        try {
            Map<String, String> answerMap = extractAnswers(formResponse.getAnswers(), questionMap);
            User patient = upsertPatient(answerMap);
            MedicalReport report = createMedicalReport(patient, formTitle, formId, responseId, formResponse, answerMap);

            record.setFormTitle(formTitle);
            record.setPatient(patient);
            record.setMedicalReport(report);
            record.setSubmittedAt(parseSubmittedAt(formResponse.getLastSubmittedTime()));
            record.setSyncStatus(GoogleFormSyncRecord.SyncStatus.SYNCED);
            record.setErrorMessage(null);
            syncRecordRepository.save(record);

            summary.syncedCount++;
        } catch (Exception e) {
            log.warn("Failed to sync formId={} responseId={} message={}", formId, responseId, e.getMessage());
            record.setFormTitle(formTitle);
            record.setSubmittedAt(parseSubmittedAt(formResponse.getLastSubmittedTime()));
            record.setSyncStatus(GoogleFormSyncRecord.SyncStatus.FAILED);
            record.setErrorMessage(trimError(e.getMessage()));
            syncRecordRepository.save(record);
            summary.failedCount++;
        }
    }

    private User upsertPatient(Map<String, String> answers) {
        String fullName = firstNonBlank(
                getByQuestionAlias(answers, "họ và tên", "ho va ten", "họ tên", "ho ten"));
        String phone = normalizePhone(firstNonBlank(
                getByQuestionAlias(answers, "số điện thoại", "so dien thoai", "điện thoại", "dien thoai")));
        String email = sanitizeEmpty(firstNonBlank(
                getByQuestionAlias(answers, "email")));

        if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
            throw new IllegalArgumentException("Thiếu thông tin định danh (phone/email) trong câu trả lời");
        }

        User user = null;
        if (phone != null && !phone.isBlank()) {
            user = userRepository.findByPhoneNumber(phone).orElse(null);
        }
        if (user == null && email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user == null) {
            user = User.builder()
                    .email(email)
                    .phoneNumber(phone)
                    .password(passwordEncoder.encode(defaultPassword))
                    .role(User.UserRole.PATIENT)
                    .isActive(true)
                    .emailVerified(false)
                    .build();
            user = userRepository.save(user);
        } else {
            if ((user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) && phone != null) {
                user.setPhoneNumber(phone);
            }
            if ((user.getEmail() == null || user.getEmail().isBlank()) && email != null) {
                user.setEmail(email);
            }
            user = userRepository.save(user);
        }

        UserInfo userInfo = user.getUserInfo();
        if (userInfo == null) {
            userInfo = UserInfo.builder().user(user).build();
        }
        if (fullName != null && !fullName.isBlank()) {
            userInfo.setFullName(fullName);
        }
        userInfoRepository.save(userInfo);
        user.setUserInfo(userInfo);
        return user;
    }

    private MedicalReport createMedicalReport(
            User patient,
            String formTitle,
            String formId,
            String responseId,
            FormResponse formResponse,
            Map<String, String> answers) {
        LocalDate reportDate = Optional.ofNullable(parseSubmittedAt(formResponse.getLastSubmittedTime()))
                .map(LocalDateTime::toLocalDate)
                .orElse(LocalDate.now());

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Thong tin tiep nhan tu Google Form:\n\n");
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String value = sanitizeEmpty(entry.getValue());
            if (value == null) {
                continue;
            }
            contentBuilder.append("- ").append(entry.getKey()).append(": ").append(value).append("\n");
        }

        String notes = "Nguon dong bo Google Forms | formTitle=" + sanitizeEmpty(formTitle)
                + " | formId=" + formId
                + " | responseId=" + responseId;

        MedicalReport report = MedicalReport.builder()
                .patient(patient)
                .type(MedicalReport.ReportType.CONSULTATION)
                .reportDate(reportDate)
                .title("Tiep nhan tu Google Form")
                .content(contentBuilder.toString())
                .notes(notes)
                .build();

        return medicalReportRepository.save(report);
    }

    private Forms createFormsService() throws IOException, GeneralSecurityException {
        try (InputStream inputStream = openCredentialsStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(List.of(
                            FormsScopes.FORMS_RESPONSES_READONLY,
                            FormsScopes.FORMS_BODY_READONLY));
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
            return new Forms.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer)
                    .setApplicationName(applicationName)
                    .build();
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        try {
            return credentialsResource.getInputStream();
        } catch (IOException primaryException) {
            // Fallback theo convention trong project hiện tại.
            Resource fallbackPrimary = new ClassPathResource("google-credentials.json");
            if (fallbackPrimary.exists()) {
                log.warn("Cannot open configured credentials at '{}'. Fallback to classpath:google-credentials.json",
                        credentialsResource);
                return fallbackPrimary.getInputStream();
            }

            Resource fallbackSecondary = new ClassPathResource("credentials.json");
            if (fallbackSecondary.exists()) {
                log.warn("Cannot open configured credentials at '{}'. Fallback to classpath:credentials.json",
                        credentialsResource);
                return fallbackSecondary.getInputStream();
            }

            throw new IOException("Khong tim thay Google credentials. "
                    + "Da thu: " + credentialsResource
                    + ", classpath:google-credentials.json, classpath:credentials.json", primaryException);
        }
    }

    private Map<String, String> extractQuestionMap(Form form) {
        // Use LinkedHashMap to preserve question order as defined in the Form
        Map<String, String> questionMap = new LinkedHashMap<>();
        if (form.getItems() == null) {
            return questionMap;
        }
        for (Item item : form.getItems()) {
            if (item.getQuestionItem() == null || item.getQuestionItem().getQuestion() == null) {
                continue;
            }
            String questionId = item.getQuestionItem().getQuestion().getQuestionId();
            if (questionId == null || questionId.isBlank()) {
                continue;
            }
            questionMap.put(questionId, item.getTitle());
        }
        return questionMap;
    }

    private Map<String, String> extractAnswers(Map<String, Answer> answerData, Map<String, String> questionMap) {
        Map<String, String> answerMap = new LinkedHashMap<>();
        if (answerData == null || answerData.isEmpty() || questionMap == null || questionMap.isEmpty()) {
            return answerMap;
        }

        // Iterate questions in form order, pick corresponding answers → đảm bảo thứ tự giống form
        for (Map.Entry<String, String> questionEntry : questionMap.entrySet()) {
            String questionId = questionEntry.getKey();
            String questionTitle = questionEntry.getValue();
            Answer rawAnswer = answerData.get(questionId);
            String answerText = extractAnswerText(rawAnswer);
            if (answerText != null && !answerText.isBlank()) {
                answerMap.put(questionTitle, answerText);
            }
        }
        return answerMap;
    }

    private String extractAnswerText(Answer answer) {
        if (answer == null || answer.getTextAnswers() == null || answer.getTextAnswers().getAnswers() == null) {
            return null;
        }
        List<String> values = new ArrayList<>();
        answer.getTextAnswers().getAnswers().forEach(a -> {
            if (a != null && a.getValue() != null && !a.getValue().isBlank()) {
                values.add(a.getValue().trim());
            }
        });
        if (values.isEmpty()) {
            return null;
        }
        return String.join(", ", values);
    }

    private List<String> parseFormIds(String formIds) {
        if (formIds == null || formIds.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(formIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private List<String> extractFormIdsFromSurveys() {
        List<Survey> surveys = surveyRepository.findAll();
        List<String> ids = new ArrayList<>();
        for (Survey survey : surveys) {
            String url = survey.getFormUrl();
            if (url == null || url.isBlank()) {
                continue;
            }
            String formId = extractFormIdFromUrl(url);
            if (formId != null && !formId.isBlank()) {
                ids.add(formId);
            }
        }
        return ids;
    }

    private String extractFormIdFromUrl(String formUrl) {
        if (formUrl == null || formUrl.isBlank()) {
            return null;
        }

        try {
            // Hỗ trợ cả dạng: /forms/d/FORM_ID/... và /forms/d/e/FORM_ID/...
            Pattern pattern = Pattern.compile("/forms/d/(?:e/)?([^/]+)");
            java.util.regex.Matcher matcher = pattern.matcher(formUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("Could not extract formId from survey url '{}': {}", formUrl, e.getMessage());
        }
        return null;
    }

    private String getByQuestionAlias(Map<String, String> answers, String... aliases) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }
        Set<String> normalizedAliases = new HashSet<>();
        for (String alias : aliases) {
            normalizedAliases.add(normalizeKey(alias));
        }

        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String normalizedKey = normalizeKey(entry.getKey());
            for (String alias : normalizedAliases) {
                if (normalizedKey.contains(alias)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private String normalizeKey(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = Pattern.compile("\\p{M}+").matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase(Locale.ROOT);
        normalized = normalized.replace("*", " ");
        normalized = normalized.replaceAll("[^a-z0-9 ]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private String normalizePhone(String phone) {
        String value = sanitizeEmpty(phone);
        if (value == null) {
            return null;
        }
        String digitsOnly = value.replaceAll("[^0-9]", "");
        if (digitsOnly.startsWith("84") && digitsOnly.length() >= 11) {
            return "0" + digitsOnly.substring(2);
        }
        return digitsOnly;
    }

    private LocalDateTime parseSubmittedAt(String submittedAt) {
        String value = sanitizeEmpty(submittedAt);
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(VIETNAM_ZONE).toLocalDateTime();
        } catch (Exception ex) {
            return null;
        }
    }

    private String sanitizeEmpty(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String value) {
        return sanitizeEmpty(value);
    }

    private String trimError(String errorMessage) {
        String value = sanitizeEmpty(errorMessage);
        if (value == null) {
            return "Unknown sync error";
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    private static class SyncSummary {
        private final String trigger;
        private final int totalForms;
        private int syncedCount;
        private int skippedDuplicate;
        private int skippedInvalid;
        private int failedCount;

        private SyncSummary(String trigger, int totalForms) {
            this.trigger = trigger;
            this.totalForms = totalForms;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            map.put("trigger", trigger);
            map.put("totalForms", totalForms);
            map.put("syncedCount", syncedCount);
            map.put("skippedDuplicate", skippedDuplicate);
            map.put("skippedInvalid", skippedInvalid);
            map.put("failedCount", failedCount);
            return map;
        }
    }
}
