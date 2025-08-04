# API Báo cáo và Thống kê

## Tổng quan
Các API RESTful cho báo cáo và thống kê dành cho ADMIN với bảo mật JWT.

## Base URL
```
/api/reports
```

## Authentication
Tất cả API đều yêu cầu JWT token trong header:
```
Authorization: Bearer <jwt_token>
```

## Phân quyền
- **Chỉ ADMIN** mới có quyền truy cập các API báo cáo
- CUSTOMER và SALES không có quyền truy cập

## Các API Endpoints

### 1. Báo cáo tổng quan doanh số
**GET** `/api/reports/sales-summary?range=month`

**Mô tả:** Báo cáo tổng quan doanh số theo tuần, tháng, quý, năm

**Query Parameters:**
- `range` (optional): Khoảng thời gian (week, month, quarter, year) - default: month

**Example Request:**
```
GET /api/reports/sales-summary?range=month
```

**Response:**
```json
{
    "success": true,
    "message": "Lấy báo cáo doanh số thành công",
    "data": {
        "period": "month",
        "totalRevenue": 15000000.00,
        "totalOrders": 25,
        "totalProducts": 4,
        "dataPoints": [
            {
                "label": "2024-01",
                "value": 5000000.00,
                "orderCount": 8
            },
            {
                "label": "2024-02",
                "value": 10000000.00,
                "orderCount": 17
            }
        ]
    }
}
```

### 2. Báo cáo top sản phẩm bán chạy
**GET** `/api/reports/top-products`

**Mô tả:** Báo cáo top 10 sản phẩm bán chạy nhất (dựa theo số lượng bán ra)

**Response:**
```json
{
    "success": true,
    "message": "Lấy báo cáo top sản phẩm thành công",
    "data": {
        "products": [
            {
                "productId": 1,
                "productName": "Laptop Dell XPS 13",
                "categoryName": "Laptop",
                "totalQuantity": 15,
                "totalRevenue": 75000000.00,
                "averagePrice": 5000000.00,
                "currentStock": 8
            },
            {
                "productId": 2,
                "productName": "iPhone 15 Pro",
                "categoryName": "Smartphone",
                "totalQuantity": 12,
                "totalRevenue": 60000000.00,
                "averagePrice": 5000000.00,
                "currentStock": 5
            }
        ]
    }
}
```

### 3. Báo cáo doanh thu theo khoảng thời gian
**GET** `/api/reports/revenue?from=2025-07-01&to=2025-07-30`

**Mô tả:** Báo cáo doanh thu chi tiết theo khoảng thời gian

**Query Parameters:**
- `from` (required): Ngày bắt đầu (format: YYYY-MM-DD)
- `to` (required): Ngày kết thúc (format: YYYY-MM-DD)

**Example Request:**
```
GET /api/reports/revenue?from=2025-07-01&to=2025-07-30
```

**Response:**
```json
{
    "success": true,
    "message": "Lấy báo cáo doanh thu thành công",
    "data": {
        "fromDate": "2025-07-01",
        "toDate": "2025-07-30",
        "totalRevenue": 25000000.00,
        "totalOrders": 35,
        "averageOrderValue": 714285.71,
        "dailyRevenues": [
            {
                "date": "2025-07-01",
                "revenue": 1500000.00,
                "orderCount": 3,
                "productCount": 8
            },
            {
                "date": "2025-07-02",
                "revenue": 2000000.00,
                "orderCount": 4,
                "productCount": 12
            }
        ]
    }
}
```

### 4. Báo cáo sản phẩm tồn kho thấp
**GET** `/api/reports/low-stock?threshold=10`

**Mô tả:** Báo cáo sản phẩm có tồn kho <= ngưỡng cảnh báo

**Query Parameters:**
- `threshold` (optional): Ngưỡng cảnh báo tồn kho - default: 10

**Example Request:**
```
GET /api/reports/low-stock?threshold=10
```

**Response:**
```json
{
    "success": true,
    "message": "Lấy báo cáo tồn kho thành công",
    "data": {
        "warningThreshold": 10,
        "totalLowStockProducts": 3,
        "products": [
            {
                "productId": 1,
                "productName": "Laptop Dell XPS 13",
                "categoryName": "Laptop",
                "currentStock": 0,
                "price": 5000000.00,
                "status": "CRITICAL"
            },
            {
                "productId": 2,
                "productName": "iPhone 15 Pro",
                "categoryName": "Smartphone",
                "currentStock": 3,
                "price": 5000000.00,
                "status": "LOW"
            },
            {
                "productId": 3,
                "productName": "Samsung Galaxy S24",
                "categoryName": "Smartphone",
                "currentStock": 8,
                "price": 4500000.00,
                "status": "WARNING"
            }
        ]
    }
}
```

## Trạng thái tồn kho

- **CRITICAL**: Hết hàng (stock = 0)
- **LOW**: Tồn kho thấp (stock <= 30% threshold)
- **WARNING**: Cảnh báo (stock <= threshold)

## Các khoảng thời gian báo cáo

- **week**: Tuần (7 ngày gần nhất)
- **month**: Tháng (30 ngày gần nhất)
- **quarter**: Quý (90 ngày gần nhất)
- **year**: Năm (365 ngày gần nhất)

## Lưu ý quan trọng

1. **Bảo mật:** Tất cả API đều được bảo vệ bằng JWT authentication
2. **Phân quyền:** Chỉ ADMIN mới có quyền truy cập các API báo cáo
3. **Dữ liệu:** Chỉ tính các đơn hàng không bị hủy (status != CANCELLED)
4. **Top products:** Chỉ lấy top 10 sản phẩm bán chạy nhất
5. **Tồn kho:** Chỉ hiển thị sản phẩm chưa bị xóa (isDeleted = false)

## Error Responses

**400 Bad Request:**
```json
{
    "success": false,
    "message": "Lỗi: Range không hợp lệ. Chỉ chấp nhận: week, month, quarter, year",
    "data": null
}
```

**401 Unauthorized:**
```json
{
    "success": false,
    "message": "Lỗi: JWT token không hợp lệ",
    "data": null
}
```

**403 Forbidden:**
```json
{
    "success": false,
    "message": "Lỗi: Chỉ ADMIN mới có quyền truy cập báo cáo này",
    "data": null
}
```

## Ví dụ sử dụng

### Báo cáo doanh số theo tháng
```bash
curl -X GET "http://localhost:8080/api/reports/sales-summary?range=month" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo top sản phẩm
```bash
curl -X GET http://localhost:8080/api/reports/top-products \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo doanh thu theo khoảng thời gian
```bash
curl -X GET "http://localhost:8080/api/reports/revenue?from=2025-07-01&to=2025-07-30" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo tồn kho thấp
```bash
curl -X GET "http://localhost:8080/api/reports/low-stock?threshold=10" \
  -H "Authorization: Bearer <admin_jwt_token>"
``` 