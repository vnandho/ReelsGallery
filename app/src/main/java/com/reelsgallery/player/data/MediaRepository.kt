package com.reelsgallery.player.data
import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.content.ContextCompat
object MediaRepository {
 private val projection=arrayOf(MediaStore.MediaColumns._ID,MediaStore.MediaColumns.DISPLAY_NAME,MediaStore.MediaColumns.MIME_TYPE,MediaStore.MediaColumns.SIZE,MediaStore.MediaColumns.DATE_ADDED,MediaStore.MediaColumns.RELATIVE_PATH,MediaStore.Video.VideoColumns.DURATION,MediaStore.Files.FileColumns.MEDIA_TYPE)
 fun requestedPermissions():Array<String> = when {
  Build.VERSION.SDK_INT>=34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
  Build.VERSION.SDK_INT>=33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO)
  else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
 }
 fun hasAnyMediaAccess(context:Context):Boolean {
  val perms=if(Build.VERSION.SDK_INT>=34) requestedPermissions().toList() else if(Build.VERSION.SDK_INT>=33) requestedPermissions().toList() else listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
  return perms.any { ContextCompat.checkSelfPermission(context,it)==PackageManager.PERMISSION_GRANTED }
 }
 fun hasFullImageAccess(context:Context)=if(Build.VERSION.SDK_INT>=33) ContextCompat.checkSelfPermission(context,Manifest.permission.READ_MEDIA_IMAGES)==PackageManager.PERMISSION_GRANTED else ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
 fun hasFullVideoAccess(context:Context)=if(Build.VERSION.SDK_INT>=33) ContextCompat.checkSelfPermission(context,Manifest.permission.READ_MEDIA_VIDEO)==PackageManager.PERMISSION_GRANTED else ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
 fun storageRoots(media:List<LocalMedia>):List<StorageRoot> = media.groupBy { it.volumeName }.map { (v,items) ->
  val primary=v==MediaStore.VOLUME_EXTERNAL_PRIMARY
  StorageRoot(v,if(primary) "Internal Storage" else "External Storage · $v",items.size,!primary)
 }.sortedBy { if(it.isRemovable) 1 else 0 }
 fun query(context:Context):List<LocalMedia> {
  val volumes=runCatching { MediaStore.getExternalVolumeNames(context) }.getOrDefault(setOf(MediaStore.VOLUME_EXTERNAL_PRIMARY))
  val out=ArrayList<LocalMedia>()
  val selection="${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
  val args=arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
  for(volume in volumes) runCatching {
   context.contentResolver.query(MediaStore.Files.getContentUri(volume),projection,selection,args,"${MediaStore.MediaColumns.DATE_ADDED} DESC")?.use { c ->
    val id=c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID); val name=c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
    val mime=c.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE); val size=c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
    val date=c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED); val path=c.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
    val dur=c.getColumnIndex(MediaStore.Video.VideoColumns.DURATION); val type=c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
    while(c.moveToNext()) {
     val video=c.getInt(type)==MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO; val mediaId=c.getLong(id)
     val base=if(video) MediaStore.Video.Media.getContentUri(volume) else MediaStore.Images.Media.getContentUri(volume)
     out+=LocalMedia(mediaId,ContentUris.withAppendedId(base,mediaId),c.getString(name)?:"Untitled",c.getString(mime)?:if(video)"video/*" else "image/*",if(c.isNull(size))0L else c.getLong(size),if(c.isNull(date))0L else c.getLong(date),if(path>=0&&!c.isNull(path))c.getString(path).orEmpty() else "",if(video&&dur>=0&&!c.isNull(dur))c.getLong(dur) else 0L,video,volume)
    }
   }
  }
  return out
 }
 fun thumbnail(context:Context,uri:android.net.Uri,edgePx:Int=420):Bitmap?=try { if(Build.VERSION.SDK_INT>=29) context.contentResolver.loadThumbnail(uri,Size(edgePx,edgePx),null) else null } catch(_:Exception){null}
}
