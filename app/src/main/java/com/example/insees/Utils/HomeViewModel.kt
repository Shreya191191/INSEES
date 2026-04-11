
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insees.Utils.FirebaseManager
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class HomeViewModel : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _profilePhoto = MutableLiveData<ByteArrayOutputStream>()
    val profilePhoto: LiveData<ByteArrayOutputStream> get() = _profilePhoto

    fun fetchUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val user = FirebaseManager.getFirebaseAuth().currentUser


            if (user != null) {
                val uid = user.uid
                val databaseRef = FirebaseManager.getFirebaseDatabase().reference
                val storageRef = FirebaseStorage.getInstance().reference

                try {
                    // 🔹 Fetch name from Realtime Database
                    val nameSnapshot = databaseRef.child("users").child(uid).get().await()
//                    Log.d("UID", uid)
                    val name = nameSnapshot.child("name").getValue(String::class.java).toString().trim()

                    _userName.postValue(name)

                    // 🔹 Fetch profile photo from Storage
                    val photoRef = storageRef.child("Profile/$uid.jpg")

                    val bytes = photoRef.getBytes(1024 * 1024).await()

                    val outputStream = ByteArrayOutputStream()
                    outputStream.write(bytes)
                    _profilePhoto.postValue(outputStream)
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error fetching user data: ${e.message}")
                } finally {
                    _loading.postValue(false)
                }
            } else {
                _loading.postValue(false)
            }
        }
    }

    fun fetchUserName() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            val user = FirebaseManager.getFirebaseAuth().currentUser

            if (user != null) {
                val uid = user.uid
                val databaseRef = FirebaseManager.getFirebaseDatabase().reference

                try {
                    // 🔹 Fetch name from Realtime Database
                    val nameSnapshot = databaseRef.child("users").child(uid).get().await()
                    Log.d("UID", uid)
                    val name = nameSnapshot.child("name").getValue(String::class.java).toString().trim()

//                    Log.d("name", "Name: $name")

                    _userName.postValue(name)
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error fetching user name: ${e.message}")
                } finally {
                    _loading.postValue(false)
                }
            } else {
                _loading.postValue(false)
            }
        }
    }
}