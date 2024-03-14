package com.fenil.asyncserviceschallenge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SampleData(
    var channel1Data: String = "",
    var channel2Data: String = "",
    var channel3Data: String = "",
    var channel4Data: String = ""
) : Parcelable