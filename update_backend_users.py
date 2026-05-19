import os

def read_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

# 1. Update Role.java
role_path = r'backend/src/main/java/com/example/backend/user/domain/Role.java'
role_content = read_file(role_path)
role_content = role_content.replace('ROLE_ANALYSTE', 'ROLE_ANALYSTE_SECURITE')
write_file(role_path, role_content)

# 2. Update UserInfoServiceImpl.java
impl_path = r'backend/src/main/java/com/example/backend/Security/service/implementation/UserInfoServiceImpl.java'
impl_content = read_file(impl_path)
# Force role on register
impl_content = impl_content.replace(
    'UserInfo userInfo= UserInfoMapper.toEntity(userInfoDto);',
    'UserInfo userInfo= UserInfoMapper.toEntity(userInfoDto);\n        userInfo.setRole(com.example.backend.user.domain.Role.ROLE_ANALYSTE_SECURITE);'
)
impl_content = impl_content.replace('ROLE_ANALYSTE', 'ROLE_ANALYSTE_SECURITE')
write_file(impl_path, impl_content)

# 3. Update UserResponse.java
resp_path = r'backend/src/main/java/com/example/backend/user/dto/UserResponse.java'
resp_content = read_file(resp_path)
resp_content = resp_content.replace('ROLE_ANALYSTE', 'ROLE_ANALYSTE_SECURITE')
write_file(resp_path, resp_content)

# 4. Add /me endpoint to UserInfoController
ctrl_path = r'backend/src/main/java/com/example/backend/Security/controller/UserInfoController.java'
ctrl_content = read_file(ctrl_path)
if 'getMe(' not in ctrl_content:
    import_sec = 'import java.security.Principal;\nimport java.util.Map;\nimport java.util.HashMap;\nimport com.example.backend.scan.repository.UserInfoRepository;\nimport com.example.backend.Security.entity.UserInfo;\n'
    ctrl_content = ctrl_content.replace('import org.springframework.web.bind.annotation.*;', import_sec + 'import org.springframework.web.bind.annotation.*;')
    
    repo_inject = '''
    @Autowired
    UserInfoRepository userInfoRepository;
'''
    ctrl_content = ctrl_content.replace('UserInfoService userInfoService;', 'UserInfoService userInfoService;\n' + repo_inject)
    
    me_endpoint = '''
    @Operation(summary = "Récupérer l'utilisateur connecté", description = "Retourne les informations de l'utilisateur basé sur son JWT.")
    @GetMapping("me")
    public ResponseEntity<?> getMe(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return userInfoRepository.findByUserName(principal.getName())
            .map(user -> {
                Map<String, Object> resp = new HashMap<>();
                resp.put("id", user.getId());
                resp.put("username", user.getUserName());
                resp.put("email", user.getEmail());
                resp.put("role", user.getRole().name());
                // Add dummy dates or real if they exist in UserInfo
                return ResponseEntity.ok(resp);
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
'''
    ctrl_content = ctrl_content.replace('public ResponseEntity<String> getUSerInfo', me_endpoint + '\n    public ResponseEntity<String> getUSerInfo')
    write_file(ctrl_path, ctrl_content)

# 5. Check AdminController.java
admin_ctrl_path = r'backend/src/main/java/com/example/backend/Security/controller/AdminController.java'
admin_content = read_file(admin_ctrl_path)
admin_content = admin_content.replace('ROLE_ANALYSTE', 'ROLE_ANALYSTE_SECURITE')
write_file(admin_ctrl_path, admin_content)

print("Java files updated successfully.")
