package com.bolsadeideas.springboot.app.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
/*import org.springframework.web.bind.annotation.RestController;*/
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.models.service.IClienteService;
import com.bolsadeideas.springboot.app.util.paginator.PageRender;

import jakarta.validation.Valid;

/*@RestController*/

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	/*
	 * @Autowired
	 * 
	 * @Qualifier("ClienteDaoJPA") private IClienteDao clienteDao;
	 */

	@Autowired
	private IClienteService clienteService;

	/*
	 * @RequestMapping(value = "/listar", method = RequestMethod.GET) public String
	 * listar(Model model) { model .addAttribute("titulo", "Listado Clientes");
	 * model.addAttribute("clientes",clienteService.findAll()); return "listar"; }
	 */

	@GetMapping(value="/ver/{id}")
	public String ver(@PathVariable(value= "id") Long id, Map<String, Object> model) {
		
		Cliente cliente = clienteService.findyOne(id);
		
		if(cliente == null) {
			return "redirect:/listar";
		}
		
		model.put("cliente", cliente);
		model.put("titulo", "Detalle Cliente: " + cliente.getNombre());
		
		return "ver";
	}

	@RequestMapping(value = "/listar", method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {

		Pageable pageRequest = PageRequest.of(page, 4);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);

		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);

		model.addAttribute("titulo", "Listado Clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}

	@GetMapping("/clientes")
	public List<Cliente> listarClientes() {
		return clienteService.findAll();
	}

	@RequestMapping(value = "/form")
	public String crear(Map<String, Object> model) {
		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		model.put("titulo", "Formulario Cliente");
		model.put("btnTitle", "Crear");
		return "form";
	}

	@RequestMapping(value = "/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model) {

		Cliente cliente;

		if (!(id > 0)) {
			return "redirect:/listar";
		}
		cliente = clienteService.findyOne(id);

		model.put("cliente", cliente);
		model.put("titulo", "Editar Cliente");
		model.put("btnTitle", "Actulizar");
		return "form";
	}

	@RequestMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id) {

		if (id > 0) {
			clienteService.delete(id);
		}

		return "redirect:/listar";
	}

	@RequestMapping(value = "/form", method = RequestMethod.POST)
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, SessionStatus status) {

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario Cliente");
			return "form";
		}

		if (!foto.isEmpty()) {
			
			String rootPath = "C://Temp//uploads";
			try {
				byte[] bytes = foto.getBytes();

				Path rutaCompleta = Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);

				cliente.setFoto(foto.getOriginalFilename());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		clienteService.save(cliente);
		status.setComplete();
		return "redirect:listar";
	}
}
