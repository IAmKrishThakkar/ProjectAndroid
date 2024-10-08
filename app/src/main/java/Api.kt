// RetrofitInstance.kt
import Model.AssignmentSubmit
import Model.Attendance

// Model/AttendanceData.kt
import com.google.gson.annotations.SerializedName
import Model.Faculty
import Model.PendingAssignment
import Model.StudentLogin
import Model.StudentTimetable
import Model.classes
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// API Interface
interface StudentApi {
    @GET("students/stud.php")
    fun getStudentsByClassId(@Query("class_id") classId: Int): Call<List<StudentLogin>>

    @GET("students/stud.php")
    fun getStudentsByStudId(@Query("stud_Id") classId: Int): Call<List<StudentLogin>>

    @GET("students/stud.php")
    fun getStudents(): Call<List<StudentLogin>>

    @GET("students/timetable.php")
    fun getTimetable(@Query("class_id") classId: Int): Call<List<StudentTimetable>>

    @GET("students/pendingAssignment.php")
    fun getPendingAssignment(@Query("class_id") classId: Int): Call<List<PendingAssignment>>

    @GET("students/pendingAssignment.php")
    fun getPendingAssignmentbyfaculty_id(@Query("faculty_id") classId: Int): Call<List<PendingAssignment>>

    @GET("students/attendance.php")
    fun getAttendance(@Query("student_id") studentId: Int): Call<List<Attendance>>

    @GET("students/faculty.php")
    fun getFaculty(): Call<List<Faculty>>

    @GET("students/classes.php")
    fun getClassesByFacultyId(@Query("faculty_id") facultyId: Int): Call<List<classes>>

    @GET("students/AssignmentSubmit.php")
    fun getAssignment(): Call<List<AssignmentSubmit>>

    @DELETE("students/pendingAssignment.php")
    suspend fun deletePendingAssignment(@Query("Ass_id") assignmentId: Int): Response<ApiResponse>

    @Multipart
    @POST("students/pendingAssignment.php")
    suspend fun postPendingAssignment(
        @Part("id")  id: RequestBody?,
        @Part("class_id")  classId: RequestBody,
        @Part("faculty_id")  facultyId: RequestBody,
        @Part("subject")  subject: RequestBody,
        @Part("description")  description: RequestBody,
        @Part("submission_deadline")  submissionDeadline: RequestBody
    ): Response<ApiResponse>

    @Multipart
    @POST("students/AssignmentSubmit.php")
    suspend fun postAssignment(
        @Part("student_id") studentId: RequestBody,
        @Part("assignment_id") assignmentId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Void>


    @POST("students/stud.php")
    @Multipart
    suspend fun resetPassword(
        @Part("email") email: RequestBody,
        @Part("newPassword") newPassword: RequestBody
    ): Response<ApiResponse>

    @POST("students/faculty.php")
    @Multipart
    suspend fun resetFacultyPassword(
        @Part("email") email: RequestBody,
        @Part("newPassword") newPassword: RequestBody
    ): Response<ApiResponse>


    @Multipart
    @POST("students/attendance.php")
    suspend fun insertAttendance(
        @Part("student_id") studentId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("date") date: RequestBody
    ): Response<ApiResponse>

    @GET("students/attendance.php")
    fun getAttendanceForDate(
        @Query("student_id") studentId: Int,
        @Query("date") date: String
    ): Call<List<Attendance>>

    @Multipart
    @POST("students/attendance.php")
    suspend fun updateAttendance(
        @Part("id") id: RequestBody,
        @Part("student_id") studentId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("date") date: RequestBody
    ): Response<ApiResponse>


}

// Retrofit instance
object RetrofitInstance {
    private const val BASE_URL = "https://netxgroup.in/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: StudentApi by lazy {
        retrofit.create(StudentApi::class.java)
    }
}
// Model/ApiResponse.kt

data class ApiResponse(
    @SerializedName("message")
    val message: String
)
