package coding_academy.crinimalintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel: ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    var crimeListLiveData = crimeRepository.getCrimes()
    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }
    fun check_data ():Boolean{

        if (crimeListLiveData.value==null)
      return  false
     else   return true

    }

}