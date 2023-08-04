package com.onlyradio.radioplayer.ui.viewmodels

//        private fun calculateTags() = viewModelScope.launch{
//
//            val response = radioSource.getAllTags()
//
//        val builder = StringBuilder()
//        val builder2 = StringBuilder()
//        val builder3 = StringBuilder()
//        val builder4 = StringBuilder()
//        val builder5 = StringBuilder()
//        val builder6 = StringBuilder()
//        val builder7 = StringBuilder()
//        val builder8 = StringBuilder()
//        val builder9 = StringBuilder()
//        val builder10 = StringBuilder()
//
//
//            var buildCount = 0
//
//            for(i in RadioSearchFragment.tagsList.indices){
//
//                if(RadioSearchFragment.tagsList[i] is TagWithGenre.Tag){
//
//                    val tagItem = RadioSearchFragment.tagsList[i] as TagWithGenre.Tag
//
//                    var count = 0
//                    var countExact = 0
//
//                    response.body()?.forEach {
//
//                        if(it.name.contains(tagItem.tag)){
//                            count += it.stationcount
//                        }
//
//                        if(it.name == tagItem.tag){
//                            countExact += it.stationcount
//                        }
//                    }
//
//                   val newTag ="TagWithGenre.Tag(\"${tagItem.tag}\", $count, $countExact), "
//
//                  if(buildCount < 50) {
//                                    buildCount++
//                builder.append(
//                   newTag
//                )
//            } else if(buildCount < 100) {
//                                    buildCount++
//            builder2.append(
//                newTag
//            )
//        } else if(buildCount < 150) {
//                                    buildCount++
//                builder3.append(
//                    newTag
//                )
//            } else if(buildCount < 200) {
//                                    buildCount++
//                builder4.append(
//                    newTag
//                )
//            } else if(buildCount < 250) {
//                 buildCount++
//                builder5.append(newTag)
//
//            } else if(buildCount < 300) {
//                      buildCount++
//                      builder6.append(
//                          newTag
//                      )
//                  } else if(buildCount < 350) {
//                      buildCount++
//                      builder7.append(
//                          newTag
//                      )
//                  } else if(buildCount < 400) {
//                      buildCount++
//                      builder8.append(
//                          newTag
//                      )
//                  } else if(buildCount < 450) {
//                      buildCount++
//                      builder9.append(
//                          newTag
//                      )
//                  } else  {
//                      buildCount++
//                      builder10.append(newTag)
//                  }
//
//                }
//            }
//
//                    Log.d("CHECKTAGS", builder.toString())
//        Log.d("CHECKTAGS", builder2.toString())
//        Log.d("CHECKTAGS", builder3.toString())
//        Log.d("CHECKTAGS", builder4.toString())
//        Log.d("CHECKTAGS", builder5.toString())
//
//            Log.d("CHECKTAGS", builder6.toString())
//            Log.d("CHECKTAGS", builder7.toString())
//            Log.d("CHECKTAGS", builder8.toString())
//            Log.d("CHECKTAGS", builder9.toString())
//            Log.d("CHECKTAGS", builder10.toString())
//
//
//        }



//init {


//        calculateTags()
//
//}



//
//private fun getAllTags () = viewModelScope.launch {
//
//    val response = radioSource.getAllTags()
//
//
//    var included = 0
//    var excluded = 0
//
//
//
//    response.body()?.let{
//
//        it.forEach{
//            included += it.stationcount
//        }
//
//
//        Log.d("CHECKTAGS", "initial size : ${it.size}")
//
//        val withoutMinors =  it.filter {
//            it.stationcount > 7
//        }
//
//        Log.d("CHECKTAGS", "minus minors : ${withoutMinors.size}")
//
//
//        val withoutObvious = mutableListOf<RadioTagsItem>()
//
//        withoutMinors.forEach { item ->
//
//            var isFind = true
//
//            for(i in listOfTags.indices){
//
//                if(item.name.contains(listOfTags[i], ignoreCase = false)){
//                    isFind = false
//                    break
//                }
//
//            }
//
//            if(isFind){
//                withoutObvious.add(item)
//            }
//        }
//
//        Log.d("CHECKTAGS", "minus included : ${withoutObvious.size}")
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
//}
//
//    init {
//
//        getAllTags()
//    }
//