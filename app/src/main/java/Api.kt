// RetrofitInstance.kt
import Model.Attendance
import Model.Faculty
import Model.PendingAssignment
import Model.StudentLogin

import Model.StudentTimetable
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// API Interface
interface StudentApi {
    @GET("students/stud.php")
    fun getStudents(): Call<List<StudentLogin>>

    @GET("students/timetable.php")
    fun getTimetable(@Query("class_id") classId: Int): Call<List<StudentTimetable>>

    @GET("students/pendingAssignment.php")
    fun getPendingAssignment(@Query("class_id") classId: Int): Call<List<PendingAssignment>>

    @GET("students/attendance.php")
    fun getAttendance(@Query("student_id") studentid: Int): Call<List<Attendance>>

    @GET("students/faculty.php")
    fun getFaculty(): Call<List<Faculty>>

    @Multipart
    @POST("students/AssignmentSubmit.php")
    suspend fun postAssignment(
        @Part("student_id") studentId: RequestBody,
        @Part("assignment_id") assignmentId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Void>
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
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: StudentApi by lazy {
        retrofit.create(StudentApi::class.java)
    }
}
