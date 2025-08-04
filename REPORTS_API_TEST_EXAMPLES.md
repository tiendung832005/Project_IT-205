# Ví dụ Test API Reports

## Chuẩn bị
1. Đăng nhập với tài khoản ADMIN để lấy JWT token
2. Đảm bảo có dữ liệu đơn hàng và sản phẩm để test

## 1. Báo cáo tổng quan doanh số

### Báo cáo theo tuần
```bash
curl -X GET "http://localhost:8080/api/reports/sales-summary?range=week" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo theo tháng
```bash
curl -X GET "http://localhost:8080/api/reports/sales-summary?range=month" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo theo quý
```bash
curl -X GET "http://localhost:8080/api/reports/sales-summary?range=quarter" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo theo năm
```bash
curl -X GET "http://localhost:8080/api/reports/sales-summary?range=year" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

## 2. Báo cáo top sản phẩm bán chạy

### Lấy top 10 sản phẩm bán chạy
```bash
curl -X GET http://localhost:8080/api/reports/top-products \
  -H "Authorization: Bearer <admin_jwt_token>"
```

## 3. Báo cáo doanh thu theo khoảng thời gian

### Báo cáo doanh thu tháng 7/2025
```bash
curl -X GET "http://localhost:8080/api/reports/revenue?from=2025-07-01&to=2025-07-31" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo doanh thu quý 3/2025
```bash
curl -X GET "http://localhost:8080/api/reports/revenue?from=2025-07-01&to=2025-09-30" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo doanh thu năm 2025
```bash
curl -X GET "http://localhost:8080/api/reports/revenue?from=2025-01-01&to=2025-12-31" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

## 4. Báo cáo sản phẩm tồn kho thấp

### Báo cáo với ngưỡng mặc định (10)
```bash
curl -X GET "http://localhost:8080/api/reports/low-stock" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo với ngưỡng tùy chỉnh (5)
```bash
curl -X GET "http://localhost:8080/api/reports/low-stock?threshold=5" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Báo cáo với ngưỡng cao (20)
```bash
curl -X GET "http://localhost:8080/api/reports/low-stock?threshold=20" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

## 5. Test các trường hợp lỗi

### Test với CUSTOMER role (không có quyền)
```bash
curl -X GET "http://localhost:8080/api/reports/sales-summary?range=month" \
  -H "Authorization: Bearer <customer_jwt_token>"
```

### Test với SALES role (không có quyền)
```bash
curl -X GET "http://localhost:8080/api/reports/top-products" \
  -H "Authorization: Bearer <sales_jwt_token>"
```

### Test range không hợp lệ
```bash
curl -X GET "http://localhost:8080/api/reports/sales-summary?range=invalid" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Test doanh thu với ngày không hợp lệ
```bash
curl -X GET "http://localhost:8080/api/reports/revenue?from=2025-07-30&to=2025-07-01" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Test thiếu tham số bắt buộc
```bash
curl -X GET "http://localhost:8080/api/reports/revenue?from=2025-07-01" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

## 6. Kiểm tra kết quả

### Kiểm tra dữ liệu báo cáo doanh số
- Tổng doanh thu có đúng không
- Số lượng đơn hàng có chính xác không
- Dữ liệu theo từng khoảng thời gian có hợp lý không

### Kiểm tra dữ liệu top sản phẩm
- Sản phẩm có được sắp xếp theo số lượng bán giảm dần không
- Chỉ có 10 sản phẩm được trả về không
- Thông tin sản phẩm có đầy đủ không

### Kiểm tra dữ liệu doanh thu
- Tổng doanh thu có đúng với tổng các ngày không
- Số lượng đơn hàng có chính xác không
- Giá trị đơn hàng trung bình có hợp lý không

### Kiểm tra dữ liệu tồn kho
- Chỉ hiển thị sản phẩm có stock <= threshold không
- Trạng thái CRITICAL/LOW/WARNING có đúng không
- Sản phẩm đã bị xóa có hiển thị không

## Lưu ý khi test

1. **Quyền truy cập:**
   - Chỉ ADMIN mới có quyền truy cập các API báo cáo
   - CUSTOMER và SALES sẽ nhận lỗi 403 Forbidden

2. **Dữ liệu:**
   - Chỉ tính các đơn hàng không bị hủy (status != CANCELLED)
   - Chỉ hiển thị sản phẩm chưa bị xóa (isDeleted = false)

3. **Khoảng thời gian:**
   - week: 7 ngày gần nhất
   - month: 30 ngày gần nhất
   - quarter: 90 ngày gần nhất
   - year: 365 ngày gần nhất

4. **Ngưỡng tồn kho:**
   - CRITICAL: stock = 0
   - LOW: stock <= 30% threshold
   - WARNING: stock <= threshold

5. **Top products:**
   - Chỉ lấy top 10 sản phẩm bán chạy nhất
   - Sắp xếp theo số lượng bán giảm dần 