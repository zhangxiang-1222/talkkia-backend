package com.talkkia.api.services;

import java.util.NoSuchElementException;

import com.talkkia.api.dto.drop.DeleteDropRequest;
import com.talkkia.api.dto.drop.DropReactionRequest;
import com.talkkia.api.dto.drop.DropReactionResponse;
import com.talkkia.api.dto.drop.DropReactionState;
import com.talkkia.api.repositories.CityRepository;
import com.talkkia.api.repositories.DropRepository;
import com.talkkia.api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Test
    void shouldIncrementAvailableDropListen() {
        when(dropRepository.incrementListen(DROP_ID)).thenReturn(7L);

        Long listen = dropService.incrementListen(DROP_ID);

        assertEquals(7L, listen);
    }

    @Test
    void shouldRejectInvalidDropIdWhenIncrementingListen() {
        assertThrows(
                IllegalArgumentException.class,
                () -> dropService.incrementListen(0L)
        );

        verifyNoInteractions(dropRepository);
    }

    @Test
    void setReactionEnablesLikeAndIncrementsLikeCount() {
        DropReactionResponse expected = new DropReactionResponse(
                DROP_ID, 5L, 2L, true, false
        );
        when(dropRepository.lockAvailableDropForReaction(DROP_ID)).thenReturn(true);
        when(dropRepository.findReactionState(USER_ID, DROP_ID))
                .thenReturn(new DropReactionState(false, false));
        when(dropRepository.upsertReaction(USER_ID, DROP_ID, true, false)).thenReturn(1);
        when(dropRepository.updateReactionCounts(
                DROP_ID, 1, 0, true, false
        )).thenReturn(expected);

        DropReactionResponse actual = dropService.setReaction(
                USER_ID,
                new DropReactionRequest(DROP_ID, "like", true)
        );

        assertSame(expected, actual);
        verify(dropRepository).upsertReaction(USER_ID, DROP_ID, true, false);
        verify(dropRepository).updateReactionCounts(DROP_ID, 1, 0, true, false);
    }

    @Test
    void setReactionDisablesFinalReactionAndDecrementsCounter() {
        DropReactionResponse expected = new DropReactionResponse(
                DROP_ID, 4L, 2L, false, false
        );
        when(dropRepository.lockAvailableDropForReaction(DROP_ID)).thenReturn(true);
        when(dropRepository.findReactionState(USER_ID, DROP_ID))
                .thenReturn(new DropReactionState(true, false));
        when(dropRepository.deleteReaction(USER_ID, DROP_ID)).thenReturn(1);
        when(dropRepository.updateReactionCounts(
                DROP_ID, -1, 0, false, false
        )).thenReturn(expected);

        DropReactionResponse actual = dropService.setReaction(
                USER_ID,
                new DropReactionRequest(DROP_ID, "like", false)
        );

        assertSame(expected, actual);
        verify(dropRepository).deleteReaction(USER_ID, DROP_ID);
        verify(dropRepository).updateReactionCounts(DROP_ID, -1, 0, false, false);
    }

    @Test
    void setReactionReturnsCurrentStateWhenRequestIsIdempotent() {
        DropReactionResponse expected = new DropReactionResponse(
                DROP_ID, 4L, 2L, false, false
        );
        when(dropRepository.lockAvailableDropForReaction(DROP_ID)).thenReturn(true);
        when(dropRepository.findReactionState(USER_ID, DROP_ID))
                .thenReturn(new DropReactionState(false, false));
        when(dropRepository.findReactionSummary(DROP_ID, false, false))
                .thenReturn(expected);

        DropReactionResponse actual = dropService.setReaction(
                USER_ID,
                new DropReactionRequest(DROP_ID, "funny", false)
        );

        assertSame(expected, actual);
        verify(dropRepository, never()).upsertReaction(USER_ID, DROP_ID, false, false);
        verify(dropRepository, never()).deleteReaction(USER_ID, DROP_ID);
        verify(dropRepository, never()).updateReactionCounts(
                DROP_ID, 0, 0, false, false
        );
    }
}
