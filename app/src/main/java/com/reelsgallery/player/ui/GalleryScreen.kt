package com.reelsgallery.player.ui
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.reelsgallery.player.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
private data class FolderEntry(val path:String,val name:String)
private sealed interface GalleryEntry { data class Folder(val folder:FolderEntry):GalleryEntry; data class File(val media:LocalMedia):GalleryEntry }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(media:List<LocalMedia>,settings:SettingsStore,refreshToken:Int,onRefresh:()->Unit,onOpenSettings:()->Unit,onOpenMedia:(LocalMedia,List<LocalMedia>)->Unit) {
 var filter by remember { mutableStateOf(GalleryFilter.ALL) }; var sort by remember { mutableStateOf(MediaSort.NEWEST) }
 var search by remember { mutableStateOf("") }; var groupByType by remember { mutableStateOf(false) }; var volume by remember { mutableStateOf<String?>(null) }
 var path by remember { mutableStateOf("") }; var sortMenu by remember { mutableStateOf(false) }; var refreshing by remember { mutableStateOf(false) }
 val columns=remember(refreshToken){settings.gridColumns}
 val scoped=remember(media,volume){media.filter{volume==null||it.volumeName==volume}}
 val visible=remember(scoped,filter,sort,search){ 
  val f=scoped.filter{(filter==GalleryFilter.ALL||(filter==GalleryFilter.VIDEOS&&it.isVideo)||(filter==GalleryFilter.PHOTOS&&!it.isVideo))&&(search.isBlank()||it.displayName.contains(search.trim(),true))}
  val ordered=when(sort){MediaSort.NEWEST->f.sortedByDescending{it.dateAddedSeconds};MediaSort.OLDEST->f.sortedBy{it.dateAddedSeconds};MediaSort.NAME->f.sortedBy{it.displayName.lowercase()};MediaSort.SIZE->f.sortedByDescending{it.sizeBytes};MediaSort.DURATION->f.sortedByDescending{it.durationMs}}
  if(filter==GalleryFilter.ALL&&groupByType) ordered.sortedBy { if(it.isVideo) 0 else 1 } else ordered
 }
 val roots=remember(visible){MediaRepository.storageRoots(visible)}
 val folders=remember(visible,path){
  visible.mapNotNull { item ->
   val p=item.relativePath.trimStart('/')
   if(!p.startsWith(path)) null else { val rest=p.removePrefix(path); val child=rest.substringBefore('/')
    if(rest.contains('/')&&child.isNotBlank()) FolderEntry(path+child.trimEnd('/')+"/",child) else null
   }
  }.distinctBy{it.path}.sortedBy{it.name.lowercase()}
 }
 val directFiles=remember(visible,path){visible.filter{it.relativePath.trimStart('/')==path}}
 val entries=remember(folders,directFiles){folders.map{GalleryEntry.Folder(it)}+directFiles.map{GalleryEntry.File(it)}}
 fun back(){ if(path.isNotEmpty()) path=path.trimEnd('/').substringBeforeLast('/',"").let{if(it.isEmpty())"" else "$it/"} else volume=null }
 BackHandler(enabled=volume!=null){back()}
 Scaffold(containerColor=MaterialTheme.colorScheme.background,topBar={
  Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=16.dp,vertical=8.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){
    if(volume!=null) TextButton(onClick={back()}){Text("‹ Back")} else Column(Modifier.weight(1f)){Text("REELS GALLERY",style=MaterialTheme.typography.titleLarge);Text("Local media library",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    if(volume!=null) Column(Modifier.weight(1f)){Text(if(path.isBlank()) roots.firstOrNull{it.volumeName==volume}?.label?:"Storage" else path.trimEnd('/').substringAfterLast('/'),maxLines=1,overflow=TextOverflow.Ellipsis);Text("${visible.size} media files",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    IconButton(onClick={refreshing=true;onRefresh()}){Text("↻",style=MaterialTheme.typography.headlineSmall)}
    IconButton(onClick=onOpenSettings){Text("⚙",style=MaterialTheme.typography.titleLarge)}
   }
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)){
    listOf(GalleryFilter.ALL,GalleryFilter.VIDEOS,GalleryFilter.PHOTOS).forEach{f->val label=when(f){GalleryFilter.ALL->"All";GalleryFilter.VIDEOS->"Videos";GalleryFilter.PHOTOS->"Images"};FilterChip(selected=filter==f,onClick={filter=f},label={Text(label)})}
    Spacer(Modifier.weight(1f))
    Box{TextButton(onClick={sortMenu=true}){Text("Sort by ▾")};DropdownMenu(expanded=sortMenu,onDismissRequest={sortMenu=false}){MediaSort.entries.filter{filter!=GalleryFilter.PHOTOS||it!=MediaSort.DURATION}.forEach{m->DropdownMenuItem(text={Text(m.label())},onClick={sort=m;sortMenu=false})};if(filter==GalleryFilter.ALL){HorizontalDivider();DropdownMenuItem(text={Text(if(groupByType) "Ungroup media" else "Group by type")},onClick={groupByType=!groupByType;sortMenu=false})}}}
   }
   OutlinedTextField(value=search,onValueChange={search=it},modifier=Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Search media")},shape=RoundedCornerShape(14.dp),trailingIcon={if(search.isNotEmpty())IconButton(onClick={search=""}){Text("✕")}})
  }
 }){padding->
  val swipe=Modifier.pointerInput(filter){var dx=0f;detectHorizontalDragGestures(onHorizontalDrag={_,d->dx+=d},onDragEnd={if(dx < -65f)filter=when(filter){GalleryFilter.ALL->GalleryFilter.VIDEOS;GalleryFilter.VIDEOS->GalleryFilter.PHOTOS;GalleryFilter.PHOTOS->GalleryFilter.PHOTOS};if(dx>65f)filter=when(filter){GalleryFilter.ALL->GalleryFilter.ALL;GalleryFilter.VIDEOS->GalleryFilter.ALL;GalleryFilter.PHOTOS->GalleryFilter.VIDEOS};dx=0f},onDragCancel={dx=0f})}
  PullToRefreshBox(isRefreshing=refreshing,onRefresh={refreshing=true;onRefresh()},modifier=Modifier.fillMaxSize().padding(padding).then(swipe)){
   LaunchedEffect(refreshToken){refreshing=false}
   if(volume==null){
    if(roots.isEmpty()) EmptyGallery("No media storage found","Allow photo and video access, then refresh.")
    else LazyVerticalGrid(GridCells.Fixed(2),Modifier.fillMaxSize().navigationBarsPadding(),contentPadding=PaddingValues(14.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
     items(roots,key={it.volumeName}){r->Card(Modifier.fillMaxWidth().clickable{volume=r.volumeName;path=""},shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(18.dp)){Text(if(r.isRemovable)"▣" else "▤",style=MaterialTheme.typography.displaySmall);Text(r.label,style=MaterialTheme.typography.titleMedium);Text("${r.itemCount} media files",color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
    }
   }else if(entries.isEmpty()) EmptyGallery("No matching media here","Try another filter or open a different folder.")
   else LazyVerticalGrid(GridCells.Fixed(columns),Modifier.fillMaxSize().navigationBarsPadding(),contentPadding=PaddingValues(12.dp),horizontalArrangement=Arrangement.spacedBy(9.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
    items(entries,key={when(it){is GalleryEntry.Folder->"folder:"+it.folder.path;is GalleryEntry.File->"file:"+it.media.uri}}){entry->when(entry){
     is GalleryEntry.Folder->Card(Modifier.fillMaxWidth().clickable{path=entry.folder.path},shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(14.dp)){Text("📁",style=MaterialTheme.typography.headlineMedium);Text(entry.folder.name,maxLines=2,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.labelLarge);Text("Folder",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
     is GalleryEntry.File->{val item=entry.media;val q=if(settings.queueScope=="ALL")visible.filter{it.isVideo}else visible.filter{it.isVideo&&it.relativePath==item.relativePath};MediaTile(item){onOpenMedia(item,q.ifEmpty{listOf(item)})}}
    }}
   }
  }
 }
}
@Composable private fun MediaTile(item:LocalMedia,onClick:()->Unit){
 val context=LocalContext.current
 val bitmap:Bitmap? by produceState<Bitmap?>(null,item.uri){value=withContext(Dispatchers.IO){MediaRepository.thumbnail(context,item.uri)}}
 Column(Modifier.fillMaxWidth().clickable(onClick=onClick)){
  Box(Modifier.fillMaxWidth().aspectRatio(.78f).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.surfaceVariant)){
   if(bitmap!=null) Image(bitmap!!.asImageBitmap(),item.displayName,Modifier.fillMaxSize(),contentScale=ContentScale.Crop) else Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(if(item.isVideo)"▶" else "▧",style=MaterialTheme.typography.headlineLarge)}
   if(item.isVideo) Surface(Modifier.align(Alignment.BottomEnd).padding(6.dp),color=Color.Black.copy(alpha=.8f),shape=RoundedCornerShape(6.dp)){Text(formatDuration(item.durationMs),Modifier.padding(6.dp),style=MaterialTheme.typography.labelSmall,color=Color.White)}
  };Spacer(Modifier.height(4.dp));Text(item.displayName,maxLines=1,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.labelMedium)
 }
}
@Composable private fun EmptyGallery(title:String,description:String){Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){Text(title,style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(8.dp));Text(description,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable fun PhotoViewer(item:LocalMedia,queue:List<LocalMedia>,startIndex:Int,onClose:()->Unit,onNavigate:(LocalMedia,Int)->Unit){
 var index by remember(item.uri,startIndex){mutableIntStateOf(startIndex.coerceIn(0,queue.lastIndex.coerceAtLeast(0)))}
 val current=queue.getOrNull(index)?:item
 BackHandler(onBack=onClose)
 Box(Modifier.fillMaxSize().background(Color.Black).pointerInput(index,queue){var total=0f;androidx.compose.foundation.gestures.detectVerticalDragGestures(onVerticalDrag={change,amount->total+=amount;change.consume()},onDragEnd={if(kotlin.math.abs(total)>70f){val next=(index+if(total<0f)1 else -1).coerceIn(0,queue.lastIndex);if(next!=index){index=next;onNavigate(queue[next],next)}};total=0f},onDragCancel={total=0f})}){
  AndroidView(modifier=Modifier.fillMaxSize(),factory={c->ImageView(c).apply{scaleType=ImageView.ScaleType.FIT_CENTER;setImageURI(current.uri)}},update={if(it.tag!=current.uri){it.setImageURI(current.uri);it.tag=current.uri}})
 }
}
private fun MediaSort.label()=when(this){MediaSort.NEWEST->"Newest";MediaSort.OLDEST->"Oldest";MediaSort.NAME->"Name";MediaSort.SIZE->"File size";MediaSort.DURATION->"Duration"}
fun formatDuration(ms:Long):String{val t=(ms/1000).coerceAtLeast(0);val h=t/3600;val m=(t%3600)/60;val s=t%60;return if(h>0)"%d:%02d:%02d".format(h,m,s) else "%d:%02d".format(m,s)}
