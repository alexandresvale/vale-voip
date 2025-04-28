package com.example.valevoip.core.extension

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Essa função verifica se uma permissão foi concedida.
 *
 * @param permission informe a permisão a ser verificada.
 * @return Boolean retorna verdadeiro ou falso.
 */
fun Activity.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Verifica se deve solicitar permissão
 */
fun Activity.shouldRequestPermission(permissions: Array<String>): Boolean {
    return permissions.any { !hasPermission(it) }
}

// Extension function para solicitar múltiplas permissões e lidar com o resultado
fun ComponentActivity.requestPermissions(
    permissions: Array<String>,
    rationale: String = "É necessário conceder permissão.",
    onGranted: () -> Unit,
    onDenied: (deniedPermissions: List<String>) -> Unit
) {
    val permissionLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val deniedPermissions = result.filterValues { !it }.keys.toList()

        if (deniedPermissions.isEmpty()) {
            // Todas as permissões foram concedidas
            onGranted()
        } else {
            // Algumas ou todas as permissões foram negadas
            onDenied(deniedPermissions)
        }
    }

    if (shouldRequestPermission(permissions)) {
        Toast.makeText(this, rationale, Toast.LENGTH_SHORT).show()
        permissionLauncher.launch(permissions)
    } else {
        // Todas as permissões já foram concedidas
        onGranted()
    }
}