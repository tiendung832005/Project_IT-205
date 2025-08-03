package com.data.project_it205.model.dto.request;

import com.data.project_it205.model.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusRequestDTO {

    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private Order.OrderStatus status;
}