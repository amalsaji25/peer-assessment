package repository;

import models.Admin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import play.db.jpa.JPAApi;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdminRepositoryTest {

    @Mock
    private JPAApi mockJPAApi;

    @Mock
    private EntityManager mockEntityManager;

    @Mock
    private TypedQuery<Admin> mockQuery;

    private AdminRepository adminRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Mock transaction behavior
        when(mockJPAApi.withTransaction(any(Function.class)))
                .then(invocation -> {
                    Function<EntityManager, Object> function = invocation.getArgument(0);
                    return function.apply(mockEntityManager);
                });

        adminRepository = new AdminRepository(mockJPAApi);
    }

    /** Test when admin exists **/
    @Test
    public void testFindAdminByUsernameShouldReturnAdminIfExists() {
        Admin mockAdmin = mock(Admin.class);

        when(mockAdmin.getUsername()).thenReturn("adminUser");

        when(mockEntityManager.createQuery(anyString(), eq(Admin.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter("username", "adminUser")).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Collections.singletonList(mockAdmin));

        Admin result = adminRepository.findAdminByUsername("adminUser");

        assertNotNull(result);
        assertEquals("adminUser", result.getUsername());
    }

    /** Test when admin does not exist **/
    @Test
    public void testFindAdminByUsernameShouldReturnNullIfNotExists() {
        when(mockEntityManager.createQuery(anyString(), eq(Admin.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter("username", "nonExistingUser")).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Collections.emptyList());

        Admin result = adminRepository.findAdminByUsername("nonExistingUser");

        assertNull(result);
    }

    /** Test when an exception occurs **/
    @Test
    public void testFindAdminByUsernameShouldHandleException() {
        when(mockEntityManager.createQuery(anyString(), eq(Admin.class))).thenThrow(new RuntimeException("DB Error"));

        Admin result = adminRepository.findAdminByUsername("adminUser");

        assertNull(result);
    }
}