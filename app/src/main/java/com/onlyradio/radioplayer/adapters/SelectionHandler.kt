package com.onlyradio.radioplayer.adapters

interface SelectionHandler {

    var selectedRadioStationId : String

    var selectedAdapterPosition : Int
    fun updateSelectedItemValues(index : Int, id : String)

    fun onNewPlayingItem(newIndex : Int, id : String)

    fun updateStationPlaybackState()

}


//class SelectedHandlerImpl() : SelectionHandler{
//
//    override  var selectedRadioStationId = ""
//
//    override var selectedAdapterPosition = -2
//    override fun updateSelectedItemValues(index : Int, id : String){
//        selectedAdapterPosition = index
//        selectedRadioStationId = id
//    }
//
//
//    override fun onNewPlayingItem(newIndex : Int, id : String){
//
//        if(selectedAdapterPosition >= 0)
//            notifyItemChanged(selectedAdapterPosition, 1f)
//        updateSelectedItemValues(newIndex, id)
//        notifyItemChanged(selectedAdapterPosition, 1)
//    }
//
//
//    override fun updateStationPlaybackState(){
//        if(selectedAdapterPosition >= 0){
//            notifyItemChanged(selectedAdapterPosition, 1)
//        }
//    }
//}