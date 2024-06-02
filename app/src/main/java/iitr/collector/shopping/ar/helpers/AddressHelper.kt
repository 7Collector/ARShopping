package iitr.collector.shopping.ar.helpers

import android.content.Context
import android.content.SharedPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import iitr.collector.shopping.ar.R

class AddressHelper(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)

    companion object {
        private const val ADDRESS_KEY = "address"
    }

    fun isAddressSaved(): Boolean {
        return sharedPreferences.contains(ADDRESS_KEY)
    }

    fun getAddress(): String? {
        return sharedPreferences.getString(ADDRESS_KEY, null)
    }

    fun saveAddress(address: String) {
        sharedPreferences.edit().putString(ADDRESS_KEY, address).apply()
    }

    fun showAddressDialog(onAddressSaved: (String) -> Unit) {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_add_address, null)
        val streetAddressEditText = view.findViewById<EditText>(R.id.editTextStreetAddress)
        val cityEditText = view.findViewById<EditText>(R.id.editTextCity)
        val stateEditText = view.findViewById<EditText>(R.id.editTextState)
        val postalCodeEditText = view.findViewById<EditText>(R.id.editTextPostalCode)
        val localityCodeEditText = view.findViewById<EditText>(R.id.editTextLocality)

        MaterialAlertDialogBuilder(context)
            .setTitle("Enter Address")
            .setView(view)
            .setPositiveButton("Save") { dialog, _ ->
                val streetAddress = streetAddressEditText.text.toString()
                val city = cityEditText.text.toString()
                val state = stateEditText.text.toString()
                val postalCode = postalCodeEditText.text.toString()
                val locality = localityCodeEditText.text.toString()

                if (streetAddress.isNotBlank() && city.isNotBlank() && state.isNotBlank() && postalCode.isNotBlank()) {
                    if (postalCode.length == 6 && postalCode.all { it.isDigit() }) {
                        val address = "$streetAddress\n$city\n$state\n$postalCode\n$locality"
                        saveAddress(address)
                        onAddressSaved(address)
                        dialog.dismiss()
                    } else {
                        postalCodeEditText.error = "Postal code should be 6 digits"
                    }
                } else {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
