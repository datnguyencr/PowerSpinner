/*
 * Designed and developed by 2019 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.powerspinnerdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skydoves.powerspinner.Option
import com.skydoves.powerspinnerdemo.databinding.ActivityCustomBinding

class CustomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.spinnerView1.setItems(
            resources.getStringArray(R.array.questions1).map { Option(it) })
        binding.spinnerView1.onItemSelected = { item ->
            binding.spinnerView1.hint = item.name
            Toast.makeText(applicationContext, item.name, Toast.LENGTH_SHORT).show()
        }
    }
}
