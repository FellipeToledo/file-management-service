package com.azvtech.file_management.controller;

import com.azvtech.file_management.exception.StorageException;
import com.azvtech.file_management.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/file")
public class FileWebController {
    private final StorageService storageService;

    @Autowired
    public FileWebController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("files", storageService.findAll());
        return "uploadForm";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        try {
            storageService.store(file);
            redirectAttributes.addFlashAttribute("message",
                    "Upload successful: " + file.getOriginalFilename());
        } catch (StorageException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/file/upload";
    }

    @PostMapping("/upload-multiple")
    public String handleMultipleFileUpload(@RequestParam("files") MultipartFile[] files,
                                           RedirectAttributes redirectAttributes) {

        try {
            if (files == null || files.length == 0) {
                redirectAttributes.addFlashAttribute("error", "No files selected");
                return "redirect:/web/file/upload";
            }

            storageService.storeMultiple(files);
            redirectAttributes.addFlashAttribute("message",
                    files.length + " files were sent successfully");
        } catch (StorageException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/file/upload";
    }

    @PostMapping("/delete")
    public String handleFileDelete(@RequestParam("originalName") String originalName,
                                   RedirectAttributes redirectAttributes) {

        try {
            storageService.delete(originalName);
            redirectAttributes.addFlashAttribute("message",
                    "File deleted successfully: " + originalName);
        } catch (StorageException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/file/upload";
    }
}
