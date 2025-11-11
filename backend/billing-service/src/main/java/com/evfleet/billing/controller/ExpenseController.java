package com.evfleet.billing.controller;

import com.evfleet.billing.dto.ExpenseDTO;
import com.evfleet.billing.entity.ExpenseCategory;
import com.evfleet.billing.entity.ExpenseStatus;
import com.evfleet.billing.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expense Management", description = "APIs for managing expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        try {
            ExpenseDTO created = expenseService.createExpense(expenseDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating expense", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<ExpenseDTO> getExpense(@PathVariable Long id) {
        try {
            ExpenseDTO expense = expenseService.getExpenseById(id);
            return ResponseEntity.ok(expense);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all expenses")
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        List<ExpenseDTO> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get expenses by entity")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<ExpenseDTO> expenses = expenseService.getExpensesByEntity(entityType, entityId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get expenses by vehicle")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByVehicle(@PathVariable Long vehicleId) {
        List<ExpenseDTO> expenses = expenseService.getExpensesByVehicle(vehicleId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get expenses by driver")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByDriver(@PathVariable Long driverId) {
        List<ExpenseDTO> expenses = expenseService.getExpensesByDriver(driverId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get expenses by category")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByCategory(@PathVariable ExpenseCategory category) {
        List<ExpenseDTO> expenses = expenseService.getExpensesByCategory(category);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get expenses by status")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByStatus(@PathVariable ExpenseStatus status) {
        List<ExpenseDTO> expenses = expenseService.getExpensesByStatus(status);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approval expenses")
    public ResponseEntity<List<ExpenseDTO>> getPendingApprovals() {
        List<ExpenseDTO> expenses = expenseService.getPendingApprovals();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/unreimbursed")
    @Operation(summary = "Get unreimbursed expenses")
    public ResponseEntity<List<ExpenseDTO>> getUnreimbursedExpenses() {
        List<ExpenseDTO> expenses = expenseService.getUnreimbursedExpenses();
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense")
    public ResponseEntity<ExpenseDTO> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO expenseDTO) {
        try {
            ExpenseDTO updated = expenseService.updateExpense(id, expenseDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating expense: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/submit")
    @Operation(summary = "Submit expense for approval")
    public ResponseEntity<ExpenseDTO> submitExpense(
            @PathVariable Long id,
            @RequestParam String submittedBy) {
        try {
            ExpenseDTO submitted = expenseService.submitExpense(id, submittedBy);
            return ResponseEntity.ok(submitted);
        } catch (RuntimeException e) {
            log.error("Error submitting expense: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve expense")
    public ResponseEntity<ExpenseDTO> approveExpense(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String approvedBy = request.get("approvedBy");
            ExpenseDTO approved = expenseService.approveExpense(id, approvedBy);
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            log.error("Error approving expense: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject expense")
    public ResponseEntity<ExpenseDTO> rejectExpense(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String rejectedBy = request.get("rejectedBy");
            String reason = request.get("reason");
            ExpenseDTO rejected = expenseService.rejectExpense(id, rejectedBy, reason);
            return ResponseEntity.ok(rejected);
        } catch (RuntimeException e) {
            log.error("Error rejecting expense: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting expense: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/analytics/total-by-category")
    @Operation(summary = "Get total expenses by category")
    public ResponseEntity<BigDecimal> getTotalByCategory(
            @RequestParam ExpenseCategory category,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        BigDecimal total = expenseService.calculateTotalByCategory(category, startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/analytics/total-by-entity")
    @Operation(summary = "Get total expenses by entity")
    public ResponseEntity<BigDecimal> getTotalByEntity(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        BigDecimal total = expenseService.calculateTotalByEntity(entityType, entityId);
        return ResponseEntity.ok(total);
    }
}
