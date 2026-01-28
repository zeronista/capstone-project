package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho Receptionist Dashboard - Phase 2
 * Handles all receptionist-specific routes with role-based authorization
 */
@Controller
@RequestMapping("/receptionist")
@PreAuthorize("hasRole('RECEPTIONIST')")
@RequiredArgsConstructor
public class ReceptionistController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    /**
     * Receptionist Dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist != null) {
            // Statistics
            List<Ticket> createdTickets = ticketService.getTicketsByCreatedByUserId(receptionist.getId());
            model.addAttribute("totalTickets", createdTickets.size());
            model.addAttribute("openTickets", createdTickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.OPEN)
                    .count());
            model.addAttribute("inProgressTickets", createdTickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.IN_PROGRESS)
                    .count());

            // Recent tickets
            model.addAttribute("recentTickets", createdTickets);
        }

        model.addAttribute("receptionist", receptionist);
        return "receptionist/dashboard";
    }

    /**
     * Appointments Management (placeholder)
     */
    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "receptionist/appointments";
    }

    /**
     * Patients Management
     */
    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        // Get all patients (role = PATIENT)
        List<User> patients = userRepository.findByRole(User.UserRole.PATIENT);
        model.addAttribute("patients", patients);
        model.addAttribute("totalPatients", patients.size());

        model.addAttribute("receptionist", receptionist);
        return "receptionist/patients";
    }

    /**
     * Reminders / Follow-ups
     */
    @GetMapping("/reminders")
    public String reminders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "receptionist/reminders";
    }

    /**
     * Tickets Management
     */
    @GetMapping("/tickets")
    public String tickets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist != null) {
            List<Ticket> tickets = ticketService.getTicketsByCreatedByUserId(receptionist.getId());
            model.addAttribute("tickets", tickets);
            model.addAttribute("totalCount", tickets.size());
            model.addAttribute("openCount", tickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.OPEN)
                    .count());
        }

        model.addAttribute("receptionist", receptionist);
        return "receptionist/tickets";
    }

    /**
     * AI Callbot / Web-to-Web Demo
     */
    @GetMapping("/callbot")
    public String callbot(Model model) {
        return "ai/web-call";
    }

    /**
     * Call History
     */
    @GetMapping("/calls")
    public String calls(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "ai/calls";
    }
}
