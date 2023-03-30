
# FreeImageSearch (무료 이미지 검색)
***Unsplash API를 활용하여 저작권 무료 이미지를 검색 및 저장할 수 있다.*** 저장한 이미지는 Wallpaper로 바로 설정할 수 있다.

![image](https://user-images.githubusercontent.com/67175445/228841384-2b25888d-7d79-4ee9-9d7e-e17d17d64e7f.png)
![image](https://user-images.githubusercontent.com/67175445/228841660-4a6c699d-ce9d-4130-bbdc-fe84efc05b3c.png)
![image](https://user-images.githubusercontent.com/67175445/228841872-c4000732-2a4c-43d6-824b-4e6fa75d51c8.png)


# Facebook Shimmer
데이터가 로딩중일 경우, 조금 더 나은 유저경험을 위해 Facebook의 Shimmer 라이브러리를 활용하였다. 사진이 로딩되기 전에는 아래와 같이 나타난다.

![image](https://user-images.githubusercontent.com/67175445/228843092-b6c441ec-0280-4685-a6d8-bf20b9482269.png)

# Glide
Glide는 다양한 기능을 제공하는데, 그 중 Thumbnail 기능을 활용하였다. ***Thumbnail을 활용하면 원래 로딩하고자 하는 큰 용량의 이미지를 로딩하기전 저화질의 이미지를 빠르게 로딩하여 유저에게 노출하고 원래의 이미지가 로딩이 되면 Thumbnail과 Swap을 함으로서 유저 경험을 향상시킬 수 있다.***

```kotlin
Glide.with(binding.root)
                .load(photo.urls?.regular)
                .thumbnail(
                    Glide.with(binding.root)
                        .load(photo.urls?.thumb)
                        .transition(DrawableTransitionOptions.withCrossFade())
                )
                .override(targetWidth, targetHeight)
                .into(binding.photoImageView)

```

Glide는 이미지 저장 기능또한 제공한다. 하지만 ***SDK 29버전 이상부터는 WRITE_EXTERNAL_STORAGE PERMISSION을 획득***해야 하며 이미지 다운로드 및 배경 화면 설정에서도 따로 처리해야할 것들이 있다.

```kotlin
private fun downloadPhoto(photoUrl: String?) {
  photoUrl ?: return

  Glide.with(this)
      .asBitmap()
      .load(photoUrl)
      .diskCacheStrategy(DiskCacheStrategy.NONE)
      .into(
          object: CustomTarget<Bitmap>(SIZE_ORIGINAL, SIZE_ORIGINAL) {
              override fun onResourceReady(
                  resource: Bitmap,
                  transition: Transition<in Bitmap>?
              ) {
                  saveBitmapToMediaStore(resource)

                  val wallPaperManager = WallpaperManager.getInstance(this@MainActivity)
                  val snackBar = Snackbar.make(binding.root, "다운로드 완료", Snackbar.LENGTH_SHORT)

                  if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&wallPaperManager.isWallpaperSupported) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && wallPaperManager.isSetWallpaperAllowed)) {
                      snackBar.setAction("배경 화면으로 저장") {
                          try {
                              wallPaperManager.setBitmap(resource)
                          } catch (exception: Exception) {
                              Snackbar.make(binding.root, "배경 화면 저장 실패", Snackbar.LENGTH_SHORT)
                          }

                      }

                      snackBar.duration = Snackbar.LENGTH_INDEFINITE
                  }

                  snackBar.show()

              }

              override fun onLoadStarted(placeholder: Drawable?) {
                  super.onLoadStarted(placeholder)
                  Snackbar.make(binding.root, "다운로드 중...", Snackbar.LENGTH_INDEFINITE).show()
              }

              override fun onLoadCleared(placeholder: Drawable?) = Unit

              override fun onLoadFailed(errorDrawable: Drawable?) {
                  super.onLoadFailed(errorDrawable)
                  Snackbar.make(binding.root, "다운로드 중...", Snackbar.LENGTH_SHORT).show()
              }

          }
      )
}

private fun saveBitmapToMediaStore(bitmap: Bitmap) {
  val fileName = "${System.currentTimeMillis()}.jpg"
  val resolver = applicationContext.contentResolver
  val imageCollectionUri =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Images.Media.getContentUri(
              MediaStore.VOLUME_EXTERNAL_PRIMARY
          )
      } else {
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI
      }

  val imageDetails = ContentValues().apply {
      put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
      put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          put(MediaStore.Images.Media.IS_PENDING, 1)
      }
  }

  val imageUri = resolver.insert(imageCollectionUri, imageDetails)
  imageUri ?: return

  resolver.openOutputStream(imageUri).use { outputStream ->
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
  }

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      imageDetails.clear()
      imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
      resolver.update(imageUri, imageDetails, null, null)
  }
}
```
