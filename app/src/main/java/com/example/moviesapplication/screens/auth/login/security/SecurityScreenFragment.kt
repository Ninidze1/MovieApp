package com.example.moviesapplication.screens.auth.login.security

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviesapplication.R
import com.example.moviesapplication.adapter.auth.SecurityRecyclerViewAdapter
import com.example.moviesapplication.base.BaseFragment
import com.example.moviesapplication.databinding.SecurityScreenFragmentBinding
import com.example.moviesapplication.entity.auth.ButtonModel
import com.example.moviesapplication.extensions.*
import com.example.moviesapplication.repository.firebase.FirebaseRepository
import com.example.moviesapplication.user_state.EncryptedSharedPref
import com.example.moviesapplication.utils.Constants.RESET_PIN
import com.example.moviesapplication.utils.Constants.UNAUTHORISED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class SecurityScreenFragment : BaseFragment<SecurityScreenFragmentBinding, SecurityScreenViewModel>(
    SecurityScreenFragmentBinding::inflate,
    SecurityScreenViewModel::class.java
) {

    private val items = mutableListOf<ButtonModel>()

    private lateinit var adapter: SecurityRecyclerViewAdapter

    private var passcode = mutableListOf<Int>()
    private val retryPasscode = mutableListOf<Int>()

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @Inject
    lateinit var sharedPref: EncryptedSharedPref
    @Inject
    lateinit var auth: FirebaseRepository

    override fun init(inflater: LayoutInflater, container: ViewGroup?) {
        biometricAuth()

        recyclerInit()
        checkLogin()
        listeners()
    }

    private fun checkLogin() {
        val pinReset = arguments?.get(RESET_PIN)
        if (pinReset != null)
            resetPasscode()

        if (sharedPref.readPasscode() == UNAUTHORISED) {
            registerPasscode()
            binding.resetButton.setGone()
        } else {
            passcode = sharedPref.readPasscode().split(',').map { it.toInt() }.toMutableList()
            loginPasscode()
        }
    }

    private fun listeners() {
        binding.resetButton.setOnClickListener {
            resetPasscode()
        }
    }

    private fun registerPasscode() {
        adapter.itemClick = { position ->

            if (passcode.size < 4)
                items[position].number?.let { digit -> addDigit(passcode, digit) }
            if (passcode.size == 4) {
                d("tagtag", "$passcode")
                sharedPref.writePasscode(passcode)
                binding.indicatortitle.text = requireActivity().getString(R.string.retry)
                loginPasscode()
            }
            if (position == 11 && passcode.isNotEmpty())
                deleteDigit(passcode)
            if (position == 9)
                requireContext().showToast("Enter passcode once")
        }
    }

    private fun loginPasscode() {
        binding.resetButton.show()
        binding.indicatorHolder.changeAll(R.drawable.number_button_shape)
        adapter.itemClick = { position ->

            if (retryPasscode.size != 4)
                items[position].number?.let { digit -> addDigit(retryPasscode, digit) }
            else retryPasscode.clear()
        if (retryPasscode.size == 4)
            matchingCheck()
        if (position == 11 && retryPasscode.isNotEmpty())
            deleteDigit(retryPasscode)
        if (position == 9)
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun resetPasscode() {
        binding.indicatortitle.text = requireActivity().getString(R.string.register)
        binding.indicatorHolder.changeAll(R.drawable.number_button_shape)
        sharedPref.deletePasscode()
        passcode.clear()
        retryPasscode.clear()
        registerPasscode()
        requireContext().showToast("Password reseted")
    }

    private fun deleteDigit(list: MutableList<Int>) {
        list.removeLast()
        binding.indicatorHolder.changeByStep(list.size, R.drawable.number_button_shape)
    }

    private fun addDigit(list: MutableList<Int>, digit: Int) {
        list.add(digit)
        binding.indicatorHolder.changeByStep(list.size - 1, R.drawable.dot_active_item)
    }

    private fun matchingCheck() {
        if (passcode == retryPasscode) {
            findNavController().navigate(R.id.action_securityScreenFragment_to_navigation_profile)
            viewModel.saveSession()
        } else {
            requireContext().showToast("Passcode doesn't match")
            retryPasscode.clear()
            binding.indicatorHolder.changeAll(R.drawable.dot_incorrect_item)
            binding.indicatorHolder.shake(R.drawable.number_button_shape)
            disableButtons()
        }
    }

    private fun disableButtons() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.viewDisableLayout.show()
            delay(910)
            binding.viewDisableLayout.setGone()

        }
    }

    private fun biometricAuth() {

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {

            @SuppressLint("SwitchIntDef")
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d("loglog", "$errorCode $errString")

                when (errorCode) {
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> requireActivity().showToast(errString as String)
                    BiometricPrompt.ERROR_LOCKOUT -> requireActivity().showToast(errString as String)
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                        requireActivity().showToast("Enable biometric authentication from Settings", Toast.LENGTH_LONG)
                    }
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                viewModel.saveSession()
                findNavController().navigate(R.id.action_securityScreenFragment_to_navigation_profile)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                retryPasscode.clear()
                binding.indicatorHolder.changeAll(R.drawable.dot_incorrect_item)
                binding.indicatorHolder.shake(R.drawable.number_button_shape)
                disableButtons()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Simple authentication")
            .setSubtitle("Log in using your fingerprint")
            .setNegativeButtonText("Use passcode")
            .build()
    }


    private fun addButtons() {
        (1..9).forEach { each ->
            items.add(ButtonModel(each))
        }
        items.apply {
            add(ButtonModel(null, R.drawable.ic_tuch_id))
            add(ButtonModel(0))
            add(ButtonModel(null, R.drawable.ic_back))
        }
    }

    private fun recyclerInit() {
        addButtons()
        adapter = SecurityRecyclerViewAdapter(items)
        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recycler.adapter = adapter
    }

}