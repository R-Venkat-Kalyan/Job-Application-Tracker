# 💼 **JobTrackrly** 🚀  
*Your Smart Job Application Assistant!*

Welcome to **JobTrackrly**, the smartest way to manage your job hunt journey.  
No more juggling spreadsheets, emails, and sticky notes — with JobTrackrly, track your applications, resumes, reminders, and progress all in one place.  

> ✨ Stay organized. Stay motivated. Land your dream job. ✨

---

## 🌟 **Key Features** 🌟

- **📊 Intuitive Dashboard**  
  Overview of job applications, status counts, and progress insights.  

- **📝 Add & Manage Jobs**  
  Log applications with job details — company, role, platform, expected CTC, and date applied.  

- **📂 Applied Jobs Archive**  
  View all applications in one place with quick access to resumes and details.  

- **✅ Shortlisted Jobs Tracker**  
  Keep track of interview invites and shortlist updates.  

- **❌ Rejections Record**  
  Historical record of rejections to spot patterns and improve future applications.  

- **⏳ No-Update Jobs Reminder**  
  Automated reminders for jobs without updates (e.g., after 14 days).  

- **📑 Resume Management**  
  Upload resumes stored securely on GitHub with unique versioning (email-date-time).  
  Database stores only resume URLs, ensuring lightweight storage.  

- **👤 Profile Update**  
  Maintain consistency in personal details across all new job applications.  

- **💬 Feedback Module**  
  One-click feedback to help improve JobTrackrly continuously.  

- **📧 Weekly & Monthly Summaries**  
  Get motivational progress reports delivered directly to your inbox.  

---

## 🛠️ **Technologies Used** 🛠️

- **Frontend**: HTML5 + Bootstrap (responsive and accessible UI)  
- **Backend**: Spring Boot (robust API and job logic)  
- **Database**: MongoDB (document-based storage)  
- **Storage**: GitHub API (secure resume storage with versioning)  
- **Authentication**: Session-based login system (secure and simple)  
- **Email Service**: Spring Mail Scheduler (for reminders & summaries)  

---

## 📚 **Getting Started**

### **Prerequisites**
Make sure you have the following installed:
- Java JDK 11 or higher  
- Maven  
- MongoDB  

### **Installation**

1. **Clone the repository**:  
git clone https://github.com/R-Venkat-Kalyan/Job-Application-Tracker

2. **Install backend dependencies**:  
mvn install

3. **Configure the environment**:  
- Set MongoDB connection URL in `application.properties`.  
- Add GitHub API token for resume uploads.  
- Configure email credentials for reminders.  

4. **Run the backend application**:  
mvn spring-boot:run

5. **Access the app**:  
Open your browser at `http://localhost:8080`  

---

## 🎯 **Usage**

1. **Sign Up / Log In**: Access your personal dashboard with session-based authentication.  
2. **Add Application**: Enter job details and upload resumes.  
3. **Track Progress**: Add status updates (applied, shortlisted, rejected, follow-up needed).  
4. **Receive Notifications**: Get email reminders ➝ never miss track of your applications.  
5. **Analyze Trends**: Review rejection data and progress over time for better results.  

---

## 🌐 **Live Demo**

- **Frontend (HTML5 + Bootstrap)**: [JobTrackrly UI](https://jobtrackrly-front-end.netlify.app/)  
- **Full App (Spring Boot + MongoDB)**: [JobTrackrly Complete](https://jobtrackrly-resumes.onrender.com/) *(May take a moment to load).*  

---

## 📬 **Contact**

Developer – [Kalyan]  
Email – [reddy.venkat.kalyan04@gmail.com]  
Portfolio – [Know More About Me !!](https://reddyvenkatkalyan.netlify.app/)  

---

> 🌟 **JobTrackrly** – Because every application deserves to be tracked smartly! 🌟
