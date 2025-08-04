package com.data.project_it205.model.dto.request;

import com.data.project_it205.model.entity.Invoice;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInvoiceStatusRequestDTO {

    @NotNull(message = "Trạng thái hóa đơn không được để trống")
    private Invoice.InvoiceStatus status;
} 