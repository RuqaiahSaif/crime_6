package coding_academy.crinimalintent
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME  = "Dialogtime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 1
private const val DATE_FORMAT = "EEE, MMM, dd"

 class CrimeFragment: Fragment() ,DataPicketFragment.Callbacks,TimePickerFragment.Callbacks {
     private lateinit var crime: Crime
     private lateinit var titleText: TextView
     private lateinit var dateButton: Button
     private lateinit var timeButton: Button
     private lateinit var solvedCheckBox: CheckBox
     private lateinit var reportButton: Button
     private lateinit var suspectButton: Button
     private lateinit var phoneButton: Button


     private val crimeListViewModel: CrimeListViewModel by lazy {
         ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
     }
     private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
         ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
     }

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         crime = Crime()
         val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
         // Toast.makeText(context, crimeId.toString(), Toast.LENGTH_SHORT).show()
         crimeDetailViewModel.loadCrime(crimeId)


     }

     override fun onCreateView(
         inflater: LayoutInflater,
         container: ViewGroup?,
         savedInstanceState: Bundle?
     ): View? {
         // return super.onCreateView(inflater, container, savedInstanceState)
         val view = inflater.inflate(R.layout.fragment_crime, container, false)
         titleText = view.findViewById(R.id.crime_title) as EditText
         dateButton = view.findViewById(R.id.crime_date) as Button
         timeButton = view.findViewById(R.id.crime_time) as Button
         solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
         reportButton = view.findViewById(R.id.crime_report) as Button
         suspectButton = view.findViewById(R.id.crime_suspect) as Button
         phoneButton= view.findViewById(R.id.phone_number) as Button

         /*       dateButton.apply {
            text = crime.date.toString()
            isEnabled = false }
            */




         return view
     }

     private fun updateUI() {
         titleText.setText(crime.title)
         dateButton.text = crime.date.toString()
         solvedCheckBox.isChecked = crime.isSolved
         if (crime.suspect.isNotEmpty()) {
             suspectButton.text = crime.suspect
         }

     }
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         when {
             resultCode != Activity.RESULT_OK -> return
             requestCode == REQUEST_CONTACT && data != null -> {
                 val contactUri: Uri? = data.data
// Specify which fields you want your query to return values for
                 val queryFields = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                     ,ContactsContract.CommonDataKinds.Phone.NUMBER)
// Perform your query - the contactUri is like a "where" clause here
                 val cursor = requireActivity().contentResolver.query(contactUri!!, queryFields, null, null, null)
                 cursor?.use {
// Verify cursor contains at least one result
                     if (it.count == 0) {
                         return
                     }
// Pull out the first column of the first row of data -
// that is your suspect's name
                     it.moveToFirst()
                     val suspect = it.getString(0)
                     crime.suspect = suspect
                     val phone_number = it.getString(1)
                     crime.phone_number = phone_number
                     crimeDetailViewModel.saveCrime(crime)
                     suspectButton.text = suspect
                 }
             }
         }
     }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

         super.onViewCreated(view, savedInstanceState)
         crimeDetailViewModel.crimeLiveData.observe(
             viewLifecycleOwner,
             Observer { crime ->
                 crime?.let {
                     this.crime = crime
                     updateUI()
                 }
             })

     }


     override fun onStart() {
         super.onStart()
         val titleWatcher = object : TextWatcher {

             override fun beforeTextChanged(
                 sequence: CharSequence?,
                 start: Int,
                 count: Int,
                 after: Int
             ) {
// This space intentionally left blank
             }


             override fun onTextChanged(
                 sequence: CharSequence?,
                 start: Int,
                 before: Int,
                 count: Int
             ) {
                 crime.title = sequence.toString()
             }

             override fun afterTextChanged(sequence: Editable?) {
// This one too
             }
         }
         titleText.addTextChangedListener(titleWatcher)

         solvedCheckBox.apply {
             setOnCheckedChangeListener { _, isChecked ->
                 crime.isSolved = isChecked
             }
         }



         dateButton.setOnClickListener {
             DataPicketFragment.newInstance(crime.date).apply {
                 setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                 show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
             }
         }
         timeButton.setOnClickListener {

             TimePickerFragment.newInstance(crime.date).apply {
                 setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                 show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
             }
         }

       reportButton.setOnClickListener {
             Intent(Intent.ACTION_SEND).apply {
                 type = "text/plain"
                 putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                 putExtra(
                     Intent.EXTRA_SUBJECT,
                     getString(R.string.crime_report_subject)
                 )
             }.also { intent ->
                 val chooserIntent =
                     Intent.createChooser(intent, getString(R.string.send_report))
                 startActivity(chooserIntent)
             }
         }
         suspectButton.apply {
             val pickContactIntent =
                 Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI).apply {
                     type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                 }
             setOnClickListener {
                 startActivityForResult(pickContactIntent, REQUEST_CONTACT)
             }
             val packageManager: PackageManager = requireActivity().packageManager
             val resolvedActivity: ResolveInfo? =
                 packageManager.resolveActivity(
                     pickContactIntent,
                     PackageManager.MATCH_DEFAULT_ONLY
                 )
             if (resolvedActivity == null) {
                 isEnabled = false
             }
         }
         phoneButton.setOnClickListener  {
             val number = crime.phone_number
             phoneButton.text = number
             val call = Uri.parse("tel:$number")
             val surf = Intent(Intent.ACTION_DIAL, call)
             startActivity(surf)
                 }
         }






override fun onStop() {
        super.onStop()

        crimeDetailViewModel.saveCrime(crime)
    }
    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }

    }

     override fun onDateSelected(date: Date) {
         crime.date = date
         updateUI()

     }

     override fun onTimeSelected(time: Date) {
         crime.date = time
         updateUI()
     }
      private fun getCrimeReport(): String {
             val solvedString = if (crime.isSolved) {
                 getString(R.string.crime_report_solved)
             } else {
                 getString(R.string.crime_report_unsolved)
             }
             var dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
             var suspect = if (crime.suspect.isBlank()) {
                 getString(R.string.crime_report_no_suspect)
             } else {
                 getString(R.string.crime_report_suspect, crime.suspect)
             }
             return getString(
                 R.string.crime_report,
                 crime.title, dateString, solvedString, suspect
             )
         }

 }


