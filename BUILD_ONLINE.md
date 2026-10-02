# ساخت آنلاین APK

## روش پیشنهادی: GitHub Actions

1. یک Repository جدید در GitHub بسازید.
2. تمام فایل‌های این پروژه را در Repository آپلود کنید.
3. به بخش **Actions** بروید.
4. Workflow با نام **Build APK** را انتخاب کنید.
5. روی **Run workflow** بزنید.
6. پس از پایان Build، در بخش **Artifacts** فایل `ClassAttendance-debug` را دانلود کنید.
7. داخل آن `app-debug.apk` قرار دارد و می‌توانید آن را روی گوشی Android 8+ نصب کنید.

Workflow در فایل:
`.github/workflows/build-apk.yml`

این Build نسخه debug است و برای نصب و تست مناسب است. برای انتشار در Google Play باید بعداً نسخه release و امضای دیجیتال اضافه شود.
