package com.talkkia.api.services;

import java.util.NoSuchElementException;

import com.talkkia.api.dto.drop.DeleteDropRequest;
import com.talkkia.api.repositories.CityRepository;
import com.talkkia.api.repositories.DropRepository;
import com.talkkia.api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropServiceImplTest {

    private static final Long USER_ID = 11L;
    private static final Long DROP_ID = 22L;

    @Mock
    private DropRepository dropRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DropServiceImpl dropService;

    @Test
    void shouldDeleteOwnedDrop() {
        when(dropRepository.deleteOwnedDrop(USER_ID, DROP_ID)).thenReturn(1);

        boolean deleted = dropService.deleteDrop(
                USER_ID,
                new DeleteDropRequest(DROP_ID)
        );

        assertTrue(deleted);
    }

    @Test
    void shouldRejectDropNotOwnedByUser() {
        when(dropRepository.deleteOwnedDrop(USER_ID, DROP_ID)).thenReturn(0);

        assertThrows(
                NoSuchElementException.class,
                () -> dropService.deleteDrop(
                        USER_ID,
                        new DeleteDropRequest(DROP_ID)
                )
        );
    }

    @Test
    void shouldRejectInvalidDropId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> dropService.deleteDrop(
                        USER_ID,
                        new DeleteDropRequest(0L)
                )
        );

        verifyNoInteractions(dropRepository);
    }

    @Test
    void shouldRejectMissingRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> dropService.deleteDrop(USER_ID, null)
        );

        verifyNoInteractions(dropRepository);
    }
}
