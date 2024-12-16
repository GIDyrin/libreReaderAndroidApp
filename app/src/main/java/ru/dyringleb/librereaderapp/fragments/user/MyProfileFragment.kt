package ru.dyringleb.librereaderapp.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.SharedPreferencesHelper
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.fragments.regauth.LoginFragment
import ru.dyringleb.librereaderapp.fragments.regauth.RegistrationFragment
import ru.dyringleb.librereaderapp.utils.isNetworkAvailable
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import ru.dyringleb.librereaderapp.utils.animateImageButtons
import ru.dyringleb.librereaderapp.utils.loadImage
import ru.dyringleb.librereaderapp.viewmodels.MeAsUserViewModel


class MyProfileFragment : Fragment() {
    private lateinit var profileImage: ImageView
    private lateinit var quitButton: ImageButton
    private lateinit var username: TextView
    private lateinit var about: TextView
    private lateinit var userReviews: TextView
    private lateinit var menuButton: ImageButton

    private lateinit var userViewModel: MeAsUserViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNetworkAvailable(requireContext())) {
            activity?.findViewById<ImageButton>(R.id.navButtonLibrary)?.performClick()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val sharedPreferences = SharedPreferencesHelper(requireContext())
        if (sharedPreferences.getAuthToken() == null) {
            changeCurrentFragment(parentFragmentManager, LoginFragment(), null)
        }

        userViewModel = MeAsUserViewModel()
        userViewModel.fetchUserData()



        val view = inflater.inflate(R.layout.fragment_user, container, false)
        profileImage = view.findViewById(R.id.profilePortrait)

        menuButton = view.findViewById(R.id.menuButton)
        quitButton = view.findViewById(R.id.quitButton)
        username = view.findViewById(R.id.username)
        about = view.findViewById(R.id.aboutMe)
        userReviews = view.findViewById(R.id.ReviewsLink)

        menuButton.setOnClickListener {
            animateImageButtons(menuButton)
            showPopupMenu(menuButton)
        }

        quitButton.setOnClickListener {
            animateImageButtons(quitButton)
            userViewModel.logout() // Вызов метода logout из ViewModel
        }

        userViewModel.logoutStatus.observe(viewLifecycleOwner) { it ->
            if (it == true) {
                sharedPreferences.clearAuthToken()
                changeCurrentFragment(parentFragmentManager, LoginFragment(), null)
            }
        }

        // Наблюдение за данными пользователя из UserViewModel
        userViewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            if (userInfo != null) {
                username.text = userInfo.username
                about.text = userInfo.about_user

                val imageUrl = "api/v1/user/${userInfo.id}/photo/"
                loadImage(this, imageUrl, profileImage)
            } else {
                Toast.makeText(requireContext(), "Данные о пользователе не найдены.", Toast.LENGTH_SHORT).show()
            }
        }

        // Наблюдение за отзывами пользователя
        userReviews.setOnClickListener {
            userViewModel.userInfo.value?.let { userInfo ->
                userViewModel.fetchUserReviews(userInfo.id)
            }
        }

        userViewModel.userReviews.observe(viewLifecycleOwner) { reviews ->
            reviews?.let {
                val bundle = Bundle()
                bundle.putParcelableArrayList("reviews", ArrayList(reviews))
                changeCurrentFragment(parentFragmentManager, UserReviewsFragment(), bundle)
            }
        }

        // Наблюдение за статусом выхода из системы
        userViewModel.logoutStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Вы успешно вышли из системы.", Toast.LENGTH_SHORT).show()
                changeCurrentFragment(parentFragmentManager, LoginFragment(), null)
            } else {
                Toast.makeText(requireContext(), "Ошибка выхода.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showPopupMenu(view: View) {
        // Создание PopupMenu
        val popup = PopupMenu(requireContext(), view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_profile, popup.menu)

        // Обработка нажатий на элементы меню
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.editProfile -> {
                    editProfile()
                    true
                }
                R.id.deleteAccount -> {
                    deleteAccount()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun editProfile() {
        val bundle = Bundle()
        bundle.putParcelable("userInfo", userViewModel.userInfo.value)
        changeCurrentFragment(parentFragmentManager, EditUserFragment(), bundle = bundle)

    }

    private fun deleteAccount() {
        showDeleteConfirmationDialog()
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Подтверждение удаления")
        builder.setMessage("Вы уверены, что хотите удалить аккаунт?")
        builder.setPositiveButton("Да") { dialog, which ->
            RetrofitClient.apiService.deleteMe().enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val sharedPreferences = SharedPreferencesHelper(requireContext())
                        sharedPreferences.clearAuthToken()
                        changeCurrentFragment(parentFragmentManager, RegistrationFragment(), null)
                        Toast.makeText(requireContext(), "Аккаунт успешно удалён!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "SOME" + response.message(), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
        builder.setNegativeButton("Нет") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}


