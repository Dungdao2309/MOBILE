# StuShare - Chia sẻ Tài liệu Học tập

**StuShare** là ứng dụng di động giúp sinh viên kết nối, chia sẻ và tìm kiếm các tài liệu học tập, đề thi chất lượng. Dự án được xây dựng theo kiến trúc hiện đại với **Kotlin** và **Jetpack Compose**.

## Tính năng chính
*  **Kho tài liệu:** Chia sẻ và lưu trữ tài liệu, đề thi.
*  **Tìm kiếm thông minh:** Tìm nhanh tài liệu theo từ khóa hoặc danh mục.
*  **Bảng xếp hạng:** Ghi nhận những người đóng góp tích cực.
*  **Tương tác:** Bình luận, báo cáo vi phạm, yêu cầu tài liệu.
*  **Tài khoản:** Đăng nhập an toàn, quản lý trang cá nhân.

##  Công nghệ sử dụng
* **Ngôn ngữ:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Kiến trúc:** MVVM + Clean Architecture
* **Backend & Cloud:** Firebase (Authentication, Firestore, Storage)
* **Database:** Room Database (Lưu trữ cục bộ)
* **Network:** Retrofit, Moshi
* **Dependency Injection:** Hilt (Dagger)

##  Giao diện (Screenshots)
| Màn hình chính | Đăng nhập | Upload Tài liệu |
|:---:|:---:|:---:|
| ![trangchu](https://github.com/user-attachments/assets/79b4e28f-0722-4363-8ec7-8f9d465afd8c) | ![dangnhap](https://github.com/user-attachments/assets/d118c3f2-9d80-4e9b-9e90-a722fdeb425f) | ![upload](https://github.com/user-attachments/assets/e2d1b074-fc7c-47f7-852c-d7b5bb9d00b1) |


##  Cài đặt & Chạy dự án

Để chạy ứng dụng trên máy cá nhân:

1.  **Clone dự án:**
    ```bash
    git clone https://github.com/dungdao2309/mobile.git
    cd mobile
    ```

2.  **Cấu hình Firebase:**
    * Tải file `google-services.json` từ Firebase Console của bạn.
    * Copy file đó vào thư mục: `app/google-services.json`.

3.  **Chạy ứng dụng:**
    * Mở **Android Studio**.
    * Đợi Gradle sync hoàn tất.
    * Nhấn **Run**.
