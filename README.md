# Kanban Board Project
REST API для планирования задач.

## Технологии
- __Язык:__ Java 17
- __Фреймворк:__ Spring Boot 3.5.3
- __База данных:__ PostgreSQL 17
- __Автоматизация работы с БД:__ JPA / Hibernate
- __Авторизация:__ Spring Security 
- __Средство сборки:__ Gradle - Groovy
- __Тестирование:__ JUnit 5, Mockito
- __Документация API:__ Swagger (OpenAPI 3)

## Основные функции
- Добавление пользователей
- Создание проектов
- Добавление колонок в проект
- Прикрепление задач на колонки проекта
- Добавление пользователей на задачи
- Добавление тегов на задачи
- Прикрепление файлов на задачи (хранение в директории сервера)
- CRUD-операции для всех сущностей
- Фильтрация всех сущностей по всем полям

## Статус проекта
Проект находится в активной стадии разработки. Реализован базовый функционал.

## Требования
- JDK 17
- Gradle - Groovy 8.14.2
- PostgreSQL 17
- IntelliJ IDEA

## Установка и запуск
1. __Клонируйте репозиторий:__
    ```
    git clone https://gitlab-stud.epolsoft.com/kanbanboardgroup/kanbanboardproject.git
    cd kanbanboardproject
    ```
2. __Настройте PostgreSQL:__
    - Установите PostgreSQL
    - Создайте схему базы данных:
      ```sql
      CREATE DATABASE KanbanBoardDB;
      
      CREATE TABLE "app_user" (
      "id" UUID PRIMARY KEY,
      "fname" VARCHAR(50) NOT NULL,
      "sname" VARCHAR(50) NOT NULL,
      "lname" VARCHAR(50),
      "birth_date" DATE,
      "position" VARCHAR(100) NOT NULL,
      "email" VARCHAR(255) UNIQUE NOT NULL,
      "password" VARCHAR(255) NOT NULL,
      "role" INTEGER DEFAULT 0 NOT NULL,
      "status" INTEGER DEFAULT 0 NOT NULL,
      "created_by" UUID REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "updated_by" UUID REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      "updated_at" TIMESTAMP
      );

      CREATE TABLE "project" (
      "id" UUID PRIMARY KEY,
      "title" VARCHAR(255) NOT NULL,
      "description" TEXT,
      "status" INTEGER DEFAULT 0 NOT NULL,
      "created_by" UUID NOT NULL REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "updated_by" UUID REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      "updated_at" TIMESTAMP
      );
         
      CREATE TABLE "project_column" (
      "id" UUID PRIMARY KEY,
      "project_id" UUID NOT NULL REFERENCES "project"("id") ON DELETE NO ACTION,
      "title" VARCHAR(255) NOT NULL,
      "description" TEXT,
      "is_default" BOOLEAN DEFAULT FALSE NOT NULL,
      "created_by" UUID NOT NULL REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "updated_by" UUID REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      "updated_at" TIMESTAMP
      );
         
      CREATE TABLE "task" (
      "id" UUID PRIMARY KEY,
      "column_id" UUID REFERENCES "project_column"("id") ON DELETE NO ACTION,
      "title" VARCHAR(255) NOT NULL,
      "description" TEXT,
      "planned_due_date" DATE,
      "status" INTEGER DEFAULT 1 NOT NULL,
      "created_by" UUID NOT NULL REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "updated_by" UUID REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      "updated_at" TIMESTAMP,
      "parameters" JSONB
      );
         
      CREATE TABLE "tag" (
      "id" UUID PRIMARY KEY,
      "name" VARCHAR(255) NOT NULL UNIQUE,
      "created_by" UUID NOT NULL REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "updated_by" UUID REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      "updated_at" TIMESTAMP
      );
         
      CREATE TABLE "task_tag" (
      "id" UUID PRIMARY KEY,
      "task_id" UUID NOT NULL REFERENCES "task"("id") ON DELETE NO ACTION,
      "tag_id" UUID NOT NULL REFERENCES "tag"("id") ON DELETE NO ACTION,
      UNIQUE ("task_id", "tag_id")
      );
         
      CREATE TABLE "document" (
      "id" UUID PRIMARY KEY,
      "task_id" UUID NOT NULL REFERENCES "task"("id") ON DELETE NO ACTION,
      "file_name" VARCHAR(255) NOT NULL,
      "file_type" VARCHAR(255) NOT NULL,
      "file_size" BIGINT NOT NULL,
      "file_path" VARCHAR(255) NOT NULL,
      "created_by" UUID NOT NULL REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "updated_by" UUID REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      "updated_at" TIMESTAMP
      );
         
      CREATE TABLE "user_task" (
      "id" UUID PRIMARY KEY,
      "user_id" UUID NOT NULL REFERENCES "app_user"("id") ON DELETE NO ACTION,
      "task_id" UUID NOT NULL REFERENCES "task"("id") ON DELETE NO ACTION,
      "time_consumed" INTEGER DEFAULT 0,
      "is_assigned" BOOLEAN DEFAULT TRUE NOT NULL,
      UNIQUE ("user_id", "task_id")
      );
       ```
      Подробное описание структуры БД см. в Wiki "DB planning"
3. __Добавьте пользователя-администратора в базу данных:__
   - Это вынужденная временная мера до момента добавления регистрации пользователя и использования токенов.
   ```sql
   INSERT INTO public.app_user(id, fname, sname, "position", email, password, role, status, created_by)
   VALUES ('e0178bc3-d477-4900-a89f-58734c4a8d6e', 'Billy', 'Herringron', 'Actor', 'BillyHerrington@gmail.com', '12345678', 1, 1, 'e0178bc3-d477-4900-a89f-58734c4a8d6e');
   ```
4. __Настройте конфигурацию:__
   - Отредактируйте файл ``src/main/resources/application.properties``:
   ```properties
   spring.application.name=KanbanBoardServer

   # Подключение к БД
   spring.datasource.url=jdbc:postgresql://localhost/KanbanBoardDB
   spring.datasource.username=postgres
   spring.datasource.password=1111
   
   # Параметры Hibernate и JPA
   spring.jpa.hibernate.ddl-auto=validate
   spring.jpa.open-in-view=false
   
   # Параметры Swagger
   springdoc.swagger-ui.path=/swagger-ui
   springdoc.swagger-ui.enabled=true
   
   # Конфигурация файлов
   upload.path=C:/Users/PC/Documents/kbn
   spring.servlet.multipart.max-file-size=10MB
   ```
   - Для подключения к БД можно использовать настойки параметров подключения из IDE:
   ```properties
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   ```
5. __Соберите проект:__
   ```
   ./gradlew clean build
   ```
6. __Запустите проект:__
   ```
   ./gradlew bootRun
   ```
7. __Доступ к API:__
   - API: ``http://localhost:8080/api``
   - Swagger UI: ``http://localhost:8080/swagger-ui.html``

## API Документация
Документация API доступна через Swagger UI по адресу /swagger-ui.html. Основные эндпоинты:
- ``GET /api/projects`` — Поиск проектов по фильтру.
- ``PUT /api/projects`` — Создание/изменение проекта.
- ``DELETE /api/projects/{projectId}`` — Удаление проекта.
- ``GET /api/columns`` — Поиск колонок по фильтру.
- ``PUT /api/projects/{projectId}/columns`` — Создание/изменение колонки.
- ``DELETE /api/columns/{columnId}`` — Удаление колонки.
- ``GET /api/tasks`` — Поиск задач по фильтру.
- ``PUT /api/projects/{projectId}/tasks`` — Создание/изменение задачи.
- ``GET /api/usersTasks`` — Поиск отношений пользователь-задача по фильтру.
- ``PUT /api/tasks/{taskId}/users/{userId}`` — Создание/изменение связи пользователь-задача.
- ``GET /api/tags`` — Поиск тегов по фильтру.
- ``PUT /api/tags`` — Создание/изменение тега.
- ``DELETE /api/tags/{tagId}`` — Удаление тега.
- ``GET /api/tasksTags`` — Поиск отношений задача-тег по фильтру.
- ``PUT /api/tasks/{taskId}/tags/{tagId}`` — Создание/изменение связи задача-тег.
- ``DELETE /api/tasksTags/{taskTagId}`` — Удаление связи задача-тег.
- ``GET /api/documents`` — Поиск записи со сведениями о файле по фильтру.
- ``GET /api/documents/{documentId}`` — Скачать файл.
- ``PUT /api/tasks/{taskId}/documents`` — Загрузка на сервер/изменение записи файла.
- ``DELETE /api/documents/{documentId}`` — Удаление файла.
- ``GET /api/users`` — Поиск пользователей по фильтру.
- ``PUT /api/users`` — Создание/изменение пользователя.
- ``PUT /api/users/{userId}/password`` — Изменение пароля пользователя.
- ``DELETE /api/users/{userId}`` — Удаление пользователя.

Подробное описание эндпоинтов см. в Wiki "API".

## Архитектура
Проект следует ___луковой___ архитектуре:
- __Пакеты:__
  - dev.ivantolkach.kanban.KanbanBoardServer.application — Реализует бизнес-логику высокого уровня и координирует взаимодействие между доменными объектами и внешними интерфейсами.
  - dev.ivantolkach.kanban.KanbanBoardServer.domain — Содержит бизнес-логику и сущности, которые представляют ядро приложения.
  - dev.ivantolkach.kanban.KanbanBoardServer.infrastructure — Инфраструктурный слой, отвечающий за взаимодействие с внешними системами и технологиями.
  - dev.ivantolkach.kanban.KanbanBoardServer.presentation — Отвечает за обработку входящих запросов и отправку ответов.
- __Паттерны:__ MVC, Repository, DTO.
- __База данных:__
  - Таблица ___project___: _id, title, description, status, created_by, updated_by, created_at, updated_at._
  - Таблица ___project_column___: _id, project_id, title, description, is_default, created_by, updated_by, created_at, updated_at._
  - Таблица ___task___: _id, column_id, title, description, planned_due_date, status, parameters, created_by, updated_by, created_at, updated_at._
  - Таблица ___document___: _id, task_id, file_name, file_type, file_size, file_path, created_by, updated_by, created_at, updated_at._
  - Таблица ___tag___: _id, name, created_by, updated_by, created_at, updated_at._
  - Таблица ___task_tag___: _id, task_id, tag_id._
  - Таблица ___app_user___: _id, fname, sname, lname, birth_date, position, email, password, role, status, created_by, updated_by, created_at, updated_at._
  - Таблица ___user_task___: _id, user_id, task_id, time_consumed, is_assigned._
    
    Подробное описание структуры БД см. в Wiki "DB planning"

## Тестирование
- __Юнит-тесты:__ JUnit 5, Mockito для тестирования сервисов.
- __Запуск тестов:__
    ```
    ./gradlew test
    ```
- __Покрытие:__ бизнес-логика приложения (сервисы).

## Ограничения
- Для тестирования и работы приложения в базу данных ___обязан___ быть загружен пользователь-админ с id: e0178bc3-d477-4900-a89f-58734c4a8d6e. Это необходимая мера до полной реализации регистрации пользователя и получения токенов пользователей.
- Для выполнения юнит-тестов необходимо явно указывать данные для подключения к БД.

## Планы развития
- Написание юнит-тестов для всего приложения
- Перевод сохранения файлов с локальной машины на облачный сервер
- Разработка пользовательской части приложения (frontend)
- Добавление регистрации пользователей с получением и работой с токенами
- Добавление аватарок пользователя
- Разбитие бизнес-логики по ролям пользователей
- Использование Liquibase
- Ведение документации по проекту
- Подключение Docker
- *Разбитие приложения на микросервисы
- *Переработка архитектуры приложения с луковой на гексагональную