package com.dicoding.storyapp.view.detail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.api.response.Story
import com.dicoding.storyapp.databinding.ActivityDetailStoryBinding
import com.dicoding.storyapp.view.ViewModelFactory

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding
    private val viewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.detail_story)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ion__chevron_back)


        val storyId = intent.getStringExtra("storyId")
        val storyPhotoUrl = intent.getStringExtra("storyPhotoUrl")

        storyPhotoUrl?.let {
            binding.ivDetailPhoto.load(it)
        }

        storyId?.let {
            viewModel.getStoryDetail(it)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.story.observe(this) { storyResponse ->
            if (!storyResponse.error!!) {
                storyResponse.story?.let { displayStoryDetails(it) }
            } else {
                binding.tvDetailName.text = getString(R.string.error_loading_story)
                binding.tvDetailDescription.text =
                    storyResponse.message ?: getString(R.string.unknown_error)
            }
        }

        viewModel.error.observe(this) { error ->
            binding.tvDetailName.text = getString(R.string.error)
            binding.tvDetailDescription.text = error
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayStoryDetails(story: Story) {
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
        binding.ivDetailPhoto.load(story.photoUrl)

        binding.ivDetailPhoto.transitionName = "photo"
        binding.tvDetailName.transitionName = "name"
        binding.tvDetailDescription.transitionName = "description"
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}