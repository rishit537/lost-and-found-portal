# Lost & Found Portal

A Java Swing desktop application for reporting, searching, and claiming lost or found items. Uses MySQL for storage.

## Prerequisites

- Java JDK 17+
- MySQL 8.0+
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (JDBC Driver)

## MySQL Connector/J Setup

1. **Download:**
   - Go to [MySQL Connector/J Downloads](https://dev.mysql.com/downloads/connector/j/).
   - Select **"Platform Independent"** from the *Operating System* dropdown.
   - Download the ZIP archive (e.g., `mysql-connector-j-9.x.x.zip`) and extract it.
   - Locate the JAR file inside the extracted folder (e.g., `mysql-connector-j-x.x.x.jar`).

2. **Add to Project:**
   - Copy the `.jar` file into the project's [`lib/`](lib/) directory (you can rename it to `mysql-connector-j.jar` or keep the versioned name).

3. **Configure in IDE (Optional):**
   - **IntelliJ IDEA:**
     - Open **File** > **Project Structure** (`Ctrl + Alt + Shift + S`).
     - Go to **Project Settings** > **Libraries**.
     - Click **+** > **Java**, browse and select the JAR file inside your [`lib/`](lib/) folder, and click **OK**.
   - **Eclipse / VS Code:**
     - Add the JAR file to your project's build path / referenced libraries.


## Database Setup

1. Run the setup script to create the database and tables:

```bash
mysql -u root -p < sql/setup.sql
```
*Executes the SQL setup script to create the database schema, tables, and seed default categories.*

2. Update your MySQL credentials in [`DatabaseConnection.java`](src/com/lostandfound/db/DatabaseConnection.java):

```java
private static final String USER = "root";
private static final String PASS = "your_password_here";
```

## Compile & Run

### 1. Compile
```bash
javac -cp "lib/*" -sourcepath src -d bin src/com/lostandfound/Main.java
```
- `-cp "lib/*"`: Includes the MySQL JDBC driver JAR in the classpath.
- `-sourcepath src`: Points to the source root; automatically resolves and compiles all dependent classes starting from `Main.java`.
- `-d bin`: Outputs the compiled `.class` files into the `bin/` directory.

### 2. Run

**Windows (CMD / PowerShell):**
```bash
java -cp "bin;lib/*" com.lostandfound.Main
```
*Runs the application by including both compiled classes (`bin`) and libraries (`lib/*`) using `;` as the classpath separator.*

**Linux / macOS:**
```bash
java -cp "bin:lib/*" com.lostandfound.Main
```
*Same as above, using `:` as the Unix classpath separator.*

