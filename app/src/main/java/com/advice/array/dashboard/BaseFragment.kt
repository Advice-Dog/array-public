package com.advice.array.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.android.inject

abstract class BaseFragment : Fragment() {

    protected val analytics: FirebaseAnalytics by inject()
    protected val firebaseCrashlytics: FirebaseCrashlytics by inject()

    protected lateinit var viewModel: DashboardViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
            ViewModelProvider(getDashboardFragment())[DashboardViewModel::class.java]
    }

    private fun getDashboardFragment(): DashboardFragment {
        var target: Fragment = this
        while (target.requireParentFragment() != null) {
            target = target.requireParentFragment()
            if (target is DashboardFragment)
                return target
        }
        error("could not find parent DashboardFragment")
    }
}