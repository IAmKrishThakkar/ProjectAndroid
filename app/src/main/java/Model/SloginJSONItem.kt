package Model

import com.google.gson.annotations.SerializedName

data class StudentLogin(
    @SerializedName("id")
    val id: Int,

    @SerializedName("roll_no")
    val roll_no: String,

    @SerializedName("name")
    val stud_name: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("email")
    val stud_email: String,

    @SerializedName("department")
    val department: String?,

    @SerializedName("year")
    val year: Int?,

    @SerializedName("class_id")
    val class_id: Int?,

    @SerializedName("date_of_birth")
    val dateOfBirth: String?, // Format: YYYY-MM-DD

    @SerializedName("semester")
    val semester: Int?,

    @SerializedName("academic_year")
    val academicYear: String?, // Format: YYYY-YYYY

    @SerializedName("current_courses")
    val currentCourses: String?,

    @SerializedName("fee_status")
    val feeStatus: String?,

    @SerializedName("profile_photo")
    val profilePhoto: String?, // URL or file path

    @SerializedName("gender")
    val gender: String?, // M/F/Other

    @SerializedName("address")
    val address: String?
)

data class StudentTimetable(
    @SerializedName("id")
    val id: Int,

    @SerializedName("faculty_id")
    val facultyId: Int,

    @SerializedName("class_id")
    val classId: Int,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("day")
    val day: String,

    @SerializedName("start_time")
    val startTime: String, // Format: HH:MM:SS

    @SerializedName("end_time")
    val endTime: String // Format: HH:MM:SS
)

data class PendingAssignment(
    @SerializedName("id")
    val id: Int,

    @SerializedName("faculty_id")
    val facultyId: Int,

    @SerializedName("class_id")
    val classId: Int,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("submission_deadline")
    val submissionDeadline: String // Format: YYYY-MM-DD HH:MM:SS
)

data class Attendance(
    @SerializedName("id")
    val id: Int,

    @SerializedName("student_id")
    val studentId: Int,

    @SerializedName("status")
    val status: Int, // Assuming status is an integer (0 or 1)

    @SerializedName("date")
    val date: String // Format: YYYY-MM-DD
)

data class Faculty(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("password")
    val password: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("department")
    val department: String?,

    @SerializedName("date_of_birth")
    val dateOfBirth: String?, // Format: YYYY-MM-DD

    @SerializedName("qualification")
    val qualification: String?,

    @SerializedName("passing_year")
    val passingYear: Int?,

    @SerializedName("profile_photo")
    val profilePhoto: String? // URL or file path
)


data class AssignmentSubmit(
    @SerializedName("id")
    val id: Int,

    @SerializedName("student_id")
    val studentId: Int,

    @SerializedName("assignment_id")
    val assignmentId: Int,

    @SerializedName("submission_time")
    val submissionTime: String, // Format: YYYY-MM-DD HH:MM:SS

    @SerializedName("fileLocation")
    val fileLocation: String // URL or file path
)

data class classes(
    @SerializedName("id")
    val id: Int,

    @SerializedName("class_name")
    val className: String,

    @SerializedName("department")
    val department: String,

    @SerializedName("year")
    val year: Int,
)