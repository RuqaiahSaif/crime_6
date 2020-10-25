package coding_academy.crinimalintent

import android.app.Application


class CriminalIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CrimeRepository.initialize(this)
    }
}
