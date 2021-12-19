package com.orlove101.android.casersapp.domain.models

import com.google.firebase.database.DataSnapshot

class SearchWaitingCarsParam(
    val fromNodeId: String?,
    val countCarsInApi: (Long) -> Unit,
    val onDataChanged: (DataSnapshot) -> Unit,
)
