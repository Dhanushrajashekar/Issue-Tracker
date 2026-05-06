package com.issuetracker.service;

import com.issuetracker.dto.CreateProjectRequest;
import com.issuetracker.model.Project;
import com.issuetracker.model.User;
import com.issuetracker.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserService userService;

    public Project createProject(CreateProjectRequest request, String ownerEmail) {
        if (projectRepository.existsByProjectKey(request.getProjectKey())) {
            throw new RuntimeException("Project key '" + request.getProjectKey() + "' is already taken");
        }

        User owner = userService.findByEmail(ownerEmail);

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setProjectKey(request.getProjectKey());
        project.setOwner(owner);

        // The creator is automatically a member
        project.getMembers().add(owner);

        return projectRepository.save(project);
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
    }

    // Projects visible to a user: any project they're a member of
    public List<Project> getUserProjects(String email) {
        User user = userService.findByEmail(email);
        return projectRepository.findByMembersContaining(user);
    }

    // All projects (admin only)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project addMember(Long projectId, String memberEmail, String requesterEmail) {
        Project project = findById(projectId);
        assertOwnerOrAdmin(project, requesterEmail);

        User member = userService.findByEmail(memberEmail);
        project.getMembers().add(member);
        return projectRepository.save(project);
    }

    public Project removeMember(Long projectId, Long memberId, String requesterEmail) {
        Project project = findById(projectId);
        assertOwnerOrAdmin(project, requesterEmail);

        User member = userService.findById(memberId);

        // Don't remove the owner from their own project
        if (project.getOwner().getId().equals(memberId)) {
            throw new RuntimeException("Cannot remove the project owner");
        }

        project.getMembers().remove(member);
        return projectRepository.save(project);
    }

    public boolean isMember(Project project, String email) {
        return project.getMembers().stream()
                .anyMatch(m -> m.getEmail().equals(email));
    }

    // Only the project owner or an admin can manage members
    private void assertOwnerOrAdmin(Project project, String requesterEmail) {
        User requester = userService.findByEmail(requesterEmail);
        boolean isOwner = project.getOwner().getEmail().equals(requesterEmail);
        boolean isAdmin = requester.getRole().name().equals("ROLE_ADMIN");
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Only the project owner or an admin can perform this action");
        }
    }
}
