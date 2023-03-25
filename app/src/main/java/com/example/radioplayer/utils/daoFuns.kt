package com.example.radioplayer.utils

//import android.util.Log
//import androidx.lifecycle.viewModelScope
//import com.example.radioplayer.data.remote.entities.RadioTagsItem
//import kotlinx.coroutines.launch
//
//
//private fun getAllTags () = viewModelScope.launch {
//
//    val response = radioSource.getAllTags()
//
//    var included = 0
//    var excluded = 0
//
//    response?.let{
//
//        it.forEach{
//            included += it.stationcount
//        }
//
//
//        Log.d("CHECKTAGS", "initial size : ${it.size}")
//
//        val withoutMinors =  it.filter {
//            it.stationcount > 10
//        }
//
//        Log.d("CHECKTAGS", "minus minors : ${withoutMinors.size}")
//
//
//        val withoutObvious = mutableListOf<RadioTagsItem>()
//
//        withoutMinors.forEach { item ->
//
//            if(
//
//                item.name.contains("radio") ||
//                item.name.contains("music") ||
//                item.name.contains("estación") ||
//                item.name.contains("fm") ||
//                item.name.contains("música") ||
//                item.name.contains("variety") ||
//                item.name.contains("various") ||
//
//
//
//                item.name.contains("hits") ||
//                item.name.contains("top") ||
//                item.name.contains("pop") ||
//                item.name.contains("rock") ||
//                item.name.contains("classic") ||
//                item.name.contains("adult contemporary") ||
//                item.name.contains("country") ||
//                item.name.contains("dance") ||
//                item.name.contains("electro") ||
//                item.name.contains("jazz") ||
//                item.name.contains("oldies") ||
//                item.name.contains("alternative") ||
//                item.name.contains("house") ||
//                item.name.contains("ambient") ||
//                item.name.contains("folk") ||
//                item.name.contains("blues") ||
//
//
//
//
//
//                item.name.contains("regional") ||
//                item.name.contains("entretenimiento") ||
//                item.name.contains("entertainment") ||
//                item.name.contains("easy listening") ||
//                item.name.contains("news") ||
//                item.name.contains("talk") ||
//                item.name.contains("speech") ||
//                item.name.contains("comedy") ||
//                item.name.contains("storytelling") ||
//                item.name.contains("podcast") ||
//                item.name.contains("culture") ||
//                item.name.contains("science") ||
//                item.name.contains("film") ||
//                item.name.contains("soundtrack") ||
//                item.name.contains("information") ||
//                item.name.contains("sport") ||
//                item.name.contains("tv") ||
//                item.name.contains("noticias") ||
//                item.name.contains("politics") ||
//                item.name.contains("education") ||
//                item.name.contains("moi merino") ||
//                item.name.contains("commercial") ||
//                item.name.contains("banda") ||
//                item.name.contains("balada") ||
//                item.name.contains("bible") ||
//                item.name.contains("christian") ||
//                item.name.contains("catholic") ||
//                item.name.contains("gospel") ||
//
//                item.name.contains("charts") ||
//                item.name.contains("chill") ||
//                item.name.contains("disco") ||
//                item.name.contains("eclectic") ||
//                item.name.contains("edm") ||
//                item.name.contains("funk") ||
//                item.name.contains("grupera") ||
//                item.name.contains("heavy metal") ||
//                item.name.contains("hip-hop") ||
//                item.name.contains("indie") ||
//                item.name.contains("instrumental") ||
//                item.name.contains("international") ||
//                item.name.contains("juvenil") ||
//                item.name.contains("kids") ||
//                item.name.contains("latin") ||
//                item.name.contains("spanish") ||
//                item.name.contains("bollywood") ||
//                item.name.contains("lounge") ||
//                item.name.contains("mainstream") ||
//                item.name.contains("metal") ||
//                item.name.contains("punk") ||
//                item.name.contains("rap") ||
//                item.name.contains("reggae") ||
//                item.name.contains("relax") ||
//                item.name.contains("religion") ||
//                item.name.contains("religious") ||
//                item.name.contains("retro") ||
//                item.name.contains("romantic") ||
//                item.name.contains("rnb") ||
//                item.name.contains("schlager") ||
//                item.name.contains("soul") ||
//                item.name.contains("techno") ||
//                item.name.contains("traffic") ||
//                item.name.contains("trance") ||
//                item.name.contains("urban") ||
//                item.name.contains("tropical") ||
//                item.name.contains("traditional") ||
//                item.name.contains("smooth") ||
//                item.name.contains("salsa") ||
//                item.name.contains("romántica") ||
//                item.name.contains("anime") ||
//                item.name.contains("children") ||
//                item.name.contains("christmas") ||
//                item.name.contains("club") ||
//                item.name.contains("contemporary") ||
//                item.name.contains("decades") ||
//                item.name.contains("downtempo") ||
//                item.name.contains("drum and bass") ||
//                item.name.contains("economics") ||
//                item.name.contains("experimental") ||
//                item.name.contains("freeform") ||
//                item.name.contains("gothic") ||
//                item.name.contains("grupero") ||
//                item.name.contains("hardcore") ||
//                item.name.contains("hiphop") ||
//                item.name.contains("industrial") ||
//                item.name.contains("lifestyle") ||
//                item.name.contains("literature") ||
//                item.name.contains("live") ||
//                item.name.contains("local") ||
//                item.name.contains("love") ||
//                item.name.contains("new wave") ||
//                item.name.contains("opera") ||
//                item.name.contains("party") ||
//                item.name.contains("r&b") ||
//                item.name.contains("salsa") ||
//                item.name.contains("independent") ||
//                item.name.contains("workout") ||
//                item.name.contains("vocal") ||
//                item.name.contains("underground") ||
//                item.name.contains("swing") ||
//                item.name.contains("meditation") ||
//                item.name.contains("nature") ||
//                item.name.contains("spiritual") ||
//                item.name.contains("slow") ||
//                item.name.contains("sleep") ||
//                item.name.contains("romance") ||
//                item.name.contains("remixes") ||
//                item.name.contains("progressive") ||
//                item.name.contains("nostalgia") ||
//                item.name.contains("new age") ||
//                item.name.contains("jungle") ||
//                item.name.contains("guitar") ||
//                item.name.contains("groove") ||
//                item.name.contains("garage") ||
//                item.name.contains("ethnic") ||
//                item.name.contains("dubstep") ||
//                item.name.contains("dj") ||
//                item.name.contains("avant-garde") ||
//                item.name.contains("acoustic") ||
//                item.name.contains("orchestral") ||
//                item.name.contains("piano") ||
//                item.name.contains("medieval") ||
//
//
//
//
//                item.name.contains("50s") ||
//                item.name.contains("60s") ||
//                item.name.contains("70s") ||
//                item.name.contains("80s") ||
//                item.name.contains("90s") ||
//                item.name.contains("00s") ||
//
//
//
//                item.name.contains("méxico") ||
//                item.name.contains("mexico") ||
//                item.name.contains("español") ||
//                item.name.contains("greek") ||
//                item.name.contains("norteamérica") ||
//                item.name.contains("latinoamérica")
//
//
//
//
//            ) {}
//
//            else {withoutObvious.add(item)}
//
//        }
//
//        Log.d("CHECKTAGS", "minus minors : ${withoutObvious.size}")
//
//
//        withoutObvious.forEach {
//
//            excluded += it.stationcount
//
//            Log.d("CHECKTAGS", "name : ${it.name}, num : ${it.stationcount})")
//
//
//        }
//
//
//        Log.d("CHECKTAGS", "included stations : $included, excluded : $excluded")
//
//    }
//
//
//}
//


//    private fun getCountries() = viewModelScope.launch {
//
//        val response = radioSource.getAllCountries()
//
//        var count = 0
//
//        val listOfCountries = mutableListOf<Country>()
//
//        val builder = StringBuilder()
//        val builder2 = StringBuilder()
//        val builder3 = StringBuilder()
//        val builder4 = StringBuilder()
//        val builder5 = StringBuilder()
//
//
//        response.forEach {
//
//            if(count < 45) {
//                count++
//                builder.append(
//                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//                )
//            } else if(count < 90) {
//            count++
//            builder2.append(
//                "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//            )
//        } else if(count < 135) {
//                count++
//                builder3.append(
//                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//                )
//            } else if(count < 175) {
//                count++
//                builder4.append(
//                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//                )
//            } else {
//                count++
//                builder5.append(
//                    "Country(\"${it.name}\", \"${it.iso_3166_1}\"), "
//                )
//            }
//
//        }
//
//        Log.d("CHECKTAGS", builder.toString())
//        Log.d("CHECKTAGS", builder2.toString())
//        Log.d("CHECKTAGS", builder3.toString())
//        Log.d("CHECKTAGS", builder4.toString())
//        Log.d("CHECKTAGS", builder5.toString())
//
//    }

//
//<?xml version="1.0" encoding="utf-8"?>
//<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
//xmlns:app="http://schemas.android.com/apk/res-auto"
//xmlns:tools="http://schemas.android.com/tools"
//android:layout_width="match_parent"
//android:layout_height="match_parent"
//app:cardCornerRadius="15dp"
//app:strokeWidth="3dp"
//app:strokeColor="@color/dialog_cardview_border"
//
//>
//
//<LinearLayout
//android:layout_width="match_parent"
//android:layout_height="wrap_content"
//android:layout_marginTop="20dp"
//android:paddingHorizontal="16dp"
//android:layout_marginBottom="90dp"
//android:orientation="vertical">
//
//<TextView
//android:id="@+id/tvTitle"
//android:layout_width="match_parent"
//android:layout_height="wrap_content"
//android:textAlignment="center"
//android:text="Select playlist or create new"
//android:textAppearance="@style/dialogTitleText"
///>
//
//<TextView
//android:id="@+id/tvHint"
//android:layout_width="match_parent"
//android:layout_height="wrap_content"
//android:textAlignment="center"
//android:text="(Also: use DragAndDrop in Favoured tab)*"
//android:fontFamily="@font/aldrich"
//android:textSize="13sp"
//android:textColor="@color/Separator"
///>
//
//<androidx.recyclerview.widget.RecyclerView
//android:layout_marginTop="16dp"
//android:id="@+id/rvPlaylists"
//android:layout_width="wrap_content"
//android:layout_height="match_parent"
//android:layout_gravity="center"
///>
//
//</LinearLayout>
//
//
//
//<TextView
//android:id="@+id/tvMessageNoPlaylists"
//android:layout_width="match_parent"
//android:layout_height="match_parent"
//android:text="No playlists yet"
//android:gravity="center"
//android:visibility="gone"
//android:textColor="@color/color_non_interactive"
//android:textSize="22sp"
//app:layout_constraintBottom_toBottomOf="parent"
//app:layout_constraintEnd_toEndOf="parent"
//app:layout_constraintStart_toStartOf="parent"
//app:layout_constraintTop_toTopOf="parent" />
//
//
//<TextView
//android:id="@+id/tvBack"
//android:layout_width="wrap_content"
//android:layout_height="wrap_content"
//android:layout_marginStart="36dp"
//android:layout_marginBottom="36dp"
//android:text="@string/dialog_back_button"
//android:layout_gravity="bottom|start"
//android:textAppearance="@style/dialogTextButtons"
///>
//
//<TextView
//android:id="@+id/tvCreateNewPlaylist"
//android:layout_width="wrap_content"
//android:layout_height="wrap_content"
//android:layout_marginEnd="36dp"
//android:layout_marginBottom="36dp"
//android:layout_gravity="bottom|end"
//android:text="@string/dialog_create_playlist"
//android:textAppearance="@style/dialogTextButtons"
///>
//
//
//</com.google.android.material.card.MaterialCardView>