# Install WSL Terminal (Skip this process if you already have WSL terminal in your computer)
## Part 1: Install WSL using Windows PoweShell or Command Prompt 
1. Open Windows PoweShell or Command Prompt as Administrator.
2. Type and run the following command in Command Prompt or Windows PowerShell and hit Enter key on the keyboard: wsl --install
   <img src = "./docs/images/PowerShell in AdminMode.jpg">
   <mig src = "./docs/images/CMD in AdminMode.jpg">
3. This will install WSL along with the default Linux distribution (Ubuntu).
4. Restart your computer after the installation completes.

# Setup the Environment (Skip this process if you already have Git in your computer)
## Part 2: Download Git using WSL terminal
1. Open WSL terminal.
2. Type the following commands below to install Git if you have not installed it in the WSL environment.
   - sudo apt update
   - sudo apt install git

## Part 3: Clone the Repository using WSL terminal
1. Open internet browser (Edge / Chrome) and go to the GitHub Repository.
2. Find the URL link for the repository you want to fork.
3. Copy the URL link.
4. Go to the WSL terminal, use the git clone command followed by the URL link of the repository you want to clone. For example: git clone https://github.com/robinyms78/gamify-api
5. Press Enter key on your keyboard.
6. Once the repository is cloned, naviagate into the project folder by typing: cd gamify-api
7. Verify the contents of the repository using this command to list all the files and folders in the cloned repository: ls
   <img src = "./docs/images/Gamify-API download successful.jpg">
8. That's it! you have successfully cloned the GitHub Repository using WSL terminal.

# Run the repository in WSL environment
## Part 4: Run the Repository using WSL terminal
1. In the WSl terminal, type this command to build the project: mvn clean install
   <img src ="./docs/images/mvn_clean_install.jpg">
2. You should see this screenshot once the installation is successful.
   <img src = "./docs/images/mvn_clean_install_successful.jpg>
3. Copy and paste the commands below onto the WSL terminal and hit Enter key on your keyboard:
    export SPRING_DATASOURCE_URL="jdbc:postgresql://ep-nameless-wave-a1ciz3q7-pooler.ap-southeast-1.aws.neon.tech/gamify-api?sslmode=require"
    export SPRING_DATASOURCE_USERNAME="gamify-api_owner"
    export SPRING_DATASOURCE_PASSWORD="npg_xGeSEv1TjHO8"
    export JWT_SECRET="5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437"
    export JWT_EXPIRATION_MS="86400000"
    export SPRING_SECURITY_USER_NAME="user"
    export SPRING_SECURITY_USER_PASSWORD="password"
4. Type the command to run the project: ./mvnw spring-boot:run
   <img src = "./docs/images/mvnw_spring-boot_run.jpg>
5. You have successfully run the project using WSL terminal.
   <img src = "./docs/images/run_spring_boot_app_successful.jpg">