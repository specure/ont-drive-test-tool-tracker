package com.cadrikmdev.core.connectivty.domain.connectivity.mobile.band

import com.cadrikmdev.core.connectivty.domain.connectivity.Band

/**
 * Contains information about cellular band and frequency available for current network
 */
data class CellBand(

    /**
     * Network channel band
     */
    val band: Int,

    /**
     * Network channel attribution for current network
     */
    val channelAttribution: CellChannelAttribution,

    /**
     * Channel Number
     * for [CellChannelAttribution.NRARFCN] NR technology - nrarfcn
     * for [CellChannelAttribution.EARFCN] LTE technology - earfcn
     * for [CellChannelAttribution.UARFCN] WCDMA technology - uarfcn
     * for [CellChannelAttribution.ARFCN] GSM technology - arfcn
     */
    val channel: Int,

    /**
     * Band name
     */
    val name: String?,

    /**
     * Download frequency in mdB
     */
    val frequencyDL: Double,

    /**
     * Download frequency in mdB
     */
    val frequencyUL: Double
) : com.cadrikmdev.core.connectivty.domain.connectivity.Band() {

    override val informalName: String
        get() = name ?: ""

    companion object {

        fun fromChannelNumber(channel: Int, channelAttribution: CellChannelAttribution): CellBand? {
            val data: CellBandData?
            val step: Double

            when (channelAttribution) {
                CellChannelAttribution.ARFCN -> {
                    data = getBandFromArfcn(channel)
                    step = 0.2
                }

                CellChannelAttribution.UARFCN -> {
                    data = getBandFromUarfcn(channel)
                    step = 0.2
                }

                CellChannelAttribution.EARFCN -> {
                    data = getBandFromEarfcn(channel)
                    step = 0.1
                }

                CellChannelAttribution.NRARFCN -> {
                    data = getBandFromNrarfcn(channel)
                    step = 0.0 // not applicable for NR cells
                }
            }

            return if (data == null) {
                null
            } else {
                CellBand(
                    band = data.band,
                    channelAttribution = channelAttribution,
                    channel = channel,
                    name = data.informalName,
                    frequencyDL = data.getFrequencyDL(step, channel),
                    frequencyUL = data.getFrequencyUL(step, channel)
                )
            }
        }

        /**
         * @param arfcn Frequency to check
         * @return GSMBand object for matched band, or NULL if invalid earfcn is passed
         */
        private fun getBandFromArfcn(arfcn: Int): CellBandData? {
            // we can't differentiate between UL and DL with umts?
            for (gsmBand in gsmBands) { // Loop through all lteBands
                if (gsmBand.containsChannel(arfcn)) { // If the band contains the earfcn then return it
                    return gsmBand
                }
            }
            return null
        }

        /**
         *
         * @param uarfcn Frequency to check
         * @return UMTSBand object for matched band, or NULL if invalid earfcn is passed
         */
        private fun getBandFromUarfcn(uarfcn: Int): CellBandData? {
            // we can't differentiate between UL and DL with umts?
            for (umtsBand in umtsBands) { // Loop through all lteBands
                if (umtsBand.containsChannel(uarfcn)) { // If the band contains the earfcn then return it
                    return umtsBand
                }
            }
            return null
        }

        /**
         * @param earfcn Frequency to check
         * @return LTEBand object for matched band, or NULL if invalid earfcn is passed
         */
        private fun getBandFromEarfcn(earfcn: Int): CellBandData? {
            if (earfcn in 1..17999) { // DL
                for (band in lteBands) { // Loop through all lteBands
                    if (band.containsDLChannel(earfcn)) { // If the band contains the earfcn then return it
                        return band
                    }
                }
            } else if (earfcn in 18000..65535) { // UL
                for (band in lteBands) { // Loop through all lteBands
                    if (band.containsULChannel(earfcn)) { // If the band contains the earfcn then return it
                        return band
                    }
                }
            }
            return null
        }

        /**
         * @param nrarfcn Frequency to check
         * @return NRBand object for matched band, or NULL if invalid nrarfcn is passed
         */
        private fun getBandFromNrarfcn(nrarfcn: Int): CellBandData? {
            // different calculation - get frequency from nrarfcn directly, then assign band
            val frequencyMHz: Double = getNrFrequencyFromNrArfcn(nrarfcn)
            if (frequencyMHz == 0.0) {
                return null
            }

            // check if a band contains this frequency
            for (band in nrBands) {
                if (band.containsULFrequency(frequencyMHz)) {
                    return band
                }
            }
            return null
        }

        private fun getNrFrequencyFromNrArfcn(nrarfcn: Int): Double {
            val frequencyOffset: Double
            val deltaFrequencyKHz: Double
            val nRefOffs: Double
            when (nrarfcn) {
                in 0..599999 -> {
                    frequencyOffset = 0.0
                    deltaFrequencyKHz = 5.0
                    nRefOffs = 0.0
                }

                in 600000..2016666 -> {
                    frequencyOffset = 3000.0
                    deltaFrequencyKHz = 15.0
                    nRefOffs = 600000.0
                }

                in 2016667..3279164 -> {
                    frequencyOffset = 24250.08
                    deltaFrequencyKHz = 60.0
                    nRefOffs = 2016667.0
                }

                else -> {
                    // invalid input
                    return 0.0
                }
            }

            // FREF = FREF-Offs + ΔFGlobal (NREF – NREF-Offs)
            return frequencyOffset + deltaFrequencyKHz / 1000f * (nrarfcn - nRefOffs)
        }
    }
}