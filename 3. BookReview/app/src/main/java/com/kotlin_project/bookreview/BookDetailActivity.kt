package com.kotlin_project.bookreview

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import com.kotlin_project.bookreview.databinding.ActivityBookDetailBinding
import com.kotlin_project.bookreview.model.Book
import com.kotlin_project.bookreview.model.Review

class BookDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookReviewDB"
        ).build()

        val model = intent.getParcelableExtra<Book>("bookModel")

        binding.titleTextView.text = model?.title.orEmpty()
        binding.descriptionTextView.text = model?.description.orEmpty()

        Glide
            .with(binding.coverImageView.context)
            .load(model?.coverSmallUrl.orEmpty())
            .into(binding.coverImageView)

        Thread {
            val review = db.reviewDao().getReview(model?.id?.toInt() ?: 0)

            runOnUiThread {
                binding.reviewEditText.setText(review?.review.orEmpty())
            }
        }.start()

        binding.saveButton.setOnClickListener {
            Thread {
                db.reviewDao().saveReview(
                    Review(model?.id?.toInt() ?: 0, binding.reviewEditText.text.toString())
                )
            }.start()

            Toast.makeText(this, "저장이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}