package se.rebeccazadig.bokholken.myAdverts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import se.rebeccazadig.bokholken.adverts.AdvertsAdapter
import se.rebeccazadig.bokholken.data.Advert
import se.rebeccazadig.bokholken.databinding.FragmentMyAdvertsBinding

class MyAdvertsFragment : Fragment() {

    private val viewModel: MyAdvertsViewModel by viewModels()
    private lateinit var binding: FragmentMyAdvertsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyAdvertsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.myAdvertsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()

        }
    }
}
