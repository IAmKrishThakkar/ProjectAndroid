// RetrofitInstance.kt
import Model.Attendance
import Model.Faculty
import Model.PendingAssignment
import Model.StudentLogin

import Model.StudentTimetable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API Interface
interface StudentApi {
    @GET("stud.php") // Ensure this path is correct
    fun getStudents(): Call<List<StudentLogin>>
    @GET("timetable.php") // Ensure this path is correct
    fun getTimetable(@Query("class_id") classId: Int): Call<List<StudentTimetable>>
    @GET("pendingAssignment.php") // Ensure this path is correct
    fun getPendingAssignment(@Query("class_id") classId: Int): Call<List<PendingAssignment>>
    @GET("attendance.php") // Ensure this path is correct
    fun getAttendance(@Query("student_id") studentid: Int): Call<List<Attendance>>
    @GET("faculty.php") // Ensure this path is correct
    fun getFaculty(): Call<List<Faculty>>
}

// Retrofit instance
object RetrofitInstance {
    private const val BASE_URL = "https://netxgroup.in/students/"

    val api: StudentApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudentApi::class.java)
    }
}
