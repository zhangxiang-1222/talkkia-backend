package com.talkkia.api.services;

import java.util.Collections;
import java.util.NoSuchElementException;

import com.talkkia.api.dto.reply.DeleteReplyRequest;
import com.talkkia.api.dto.reply.ReplyReactionRequest;
import com.talkkia.api.dto.reply.ReplyReactionResponse;
import com.talkkia.api.entity.DropReply;
import com.talkkia.api.repositories.DropReplyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropReplyServiceImplTest {

    private static final Long USER_ID = 11L;
    private static final Long REPLY_ID = 22L;
    private static final Long DROP_ID = 33L;
    private static final Long PARENT_REPLY_ID = 44L;

    @Mock
    private DropReplyRepository dropReplyRepository;

    @InjectMocks
    private DropReplyServiceImpl dropReplyService;

    @Test
    void shouldFetchTopLevelRepliesInLatestOrder() {
        when(dropReplyRepository.isDropAvailable(DROP_ID)).thenReturn(true);
        when(dropReplyRepository.countTopLevelReplies(DROP_ID)).thenReturn(0L);
        when(dropReplyRepository.findTopLevelReplies(
                USER_ID,
                DROP_ID,
                10,
                0L
        )).thenReturn(Collections.emptyList());

        dropReplyService.findTopLevelReplies(
                USER_ID,
                DROP_ID,
                0,
                10
        );

        verify(dropReplyRepository).findTopLevelReplies(
                USER_ID,
                DROP_ID,
                10,
                0L
        );
    }

    @Test
    void shouldAddLike() {
        ReplyReactionResponse currentState =
                new ReplyReactionResponse(REPLY_ID, 2L, 1L, null);
        ReplyReactionResponse updatedState =
                new ReplyReactionResponse(REPLY_ID, 3L, 1L, "like");
        when(dropReplyRepository.lockAvailableReplyForReaction(REPLY_ID))
                .thenReturn(true);
        when(dropReplyRepository.findReactionState(USER_ID, REPLY_ID))
                .thenReturn(currentState);
        when(dropReplyRepository.upsertReaction(USER_ID, REPLY_ID, "like"))
                .thenReturn(1);
        when(dropReplyRepository.updateReactionCounts(
                REPLY_ID, 1, 0, "like"))
                .thenReturn(updatedState);

        ReplyReactionResponse result = dropReplyService.setReaction(
                USER_ID,
                new ReplyReactionRequest(REPLY_ID, "like")
        );

        assertEquals(updatedState, result);
        verify(dropReplyRepository, never()).deleteReaction(USER_ID, REPLY_ID);
    }

    @Test
    void shouldSwitchLikeToDislike() {
        ReplyReactionResponse currentState =
                new ReplyReactionResponse(REPLY_ID, 3L, 1L, "like");
        ReplyReactionResponse updatedState =
                new ReplyReactionResponse(REPLY_ID, 2L, 2L, "dislike");
        when(dropReplyRepository.lockAvailableReplyForReaction(REPLY_ID))
                .thenReturn(true);
        when(dropReplyRepository.findReactionState(USER_ID, REPLY_ID))
                .thenReturn(currentState);
        when(dropReplyRepository.upsertReaction(USER_ID, REPLY_ID, "dislike"))
                .thenReturn(1);
        when(dropReplyRepository.updateReactionCounts(
                REPLY_ID, -1, 1, "dislike"))
                .thenReturn(updatedState);

        ReplyReactionResponse result = dropReplyService.setReaction(
                USER_ID,
                new ReplyReactionRequest(REPLY_ID, "dislike")
        );

        assertEquals(updatedState, result);
    }

    @Test
    void shouldRemoveDislike() {
        ReplyReactionResponse currentState =
                new ReplyReactionResponse(REPLY_ID, 2L, 2L, "dislike");
        ReplyReactionResponse updatedState =
                new ReplyReactionResponse(REPLY_ID, 2L, 1L, null);
        when(dropReplyRepository.lockAvailableReplyForReaction(REPLY_ID))
                .thenReturn(true);
        when(dropReplyRepository.findReactionState(USER_ID, REPLY_ID))
                .thenReturn(currentState);
        when(dropReplyRepository.deleteReaction(USER_ID, REPLY_ID))
                .thenReturn(1);
        when(dropReplyRepository.updateReactionCounts(
                REPLY_ID, 0, -1, null))
                .thenReturn(updatedState);

        ReplyReactionResponse result = dropReplyService.setReaction(
                USER_ID,
                new ReplyReactionRequest(REPLY_ID, null)
        );

        assertEquals(updatedState, result);
    }

    @Test
    void shouldReturnCurrentStateWhenReactionIsUnchanged() {
        ReplyReactionResponse currentState =
                new ReplyReactionResponse(REPLY_ID, 3L, 1L, "like");
        when(dropReplyRepository.lockAvailableReplyForReaction(REPLY_ID))
                .thenReturn(true);
        when(dropReplyRepository.findReactionState(USER_ID, REPLY_ID))
                .thenReturn(currentState);

        ReplyReactionResponse result = dropReplyService.setReaction(
                USER_ID,
                new ReplyReactionRequest(REPLY_ID, " LIKE ")
        );

        assertEquals(currentState, result);
        verify(dropReplyRepository).lockAvailableReplyForReaction(REPLY_ID);
        verify(dropReplyRepository).findReactionState(USER_ID, REPLY_ID);
        verifyNoMoreInteractions(dropReplyRepository);
    }

    @Test
    void shouldRejectUnavailableReply() {
        when(dropReplyRepository.lockAvailableReplyForReaction(REPLY_ID))
                .thenReturn(false);

        assertThrows(
                NoSuchElementException.class,
                () -> dropReplyService.setReaction(
                        USER_ID,
                        new ReplyReactionRequest(REPLY_ID, "like")
                )
        );
    }

    @Test
    void shouldRejectInvalidReaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> dropReplyService.setReaction(
                        USER_ID,
                        new ReplyReactionRequest(REPLY_ID, "love")
                )
        );

        verifyNoMoreInteractions(dropReplyRepository);
    }

    @Test
    void shouldDeleteApprovedChildReplyAndUpdateCounts() {
        DropReply reply = createReply("approved", PARENT_REPLY_ID);
        when(dropReplyRepository.findOwnedReplyForUpdate(USER_ID, REPLY_ID))
                .thenReturn(reply);
        when(dropReplyRepository.softDeleteReply(USER_ID, REPLY_ID))
                .thenReturn(1);
        when(dropReplyRepository.decrementDropReplyCount(DROP_ID))
                .thenReturn(1);
        when(dropReplyRepository.decrementChildReplyCount(PARENT_REPLY_ID))
                .thenReturn(1);

        boolean deleted = dropReplyService.deleteReply(
                USER_ID,
                new DeleteReplyRequest(REPLY_ID)
        );

        assertTrue(deleted);
        verify(dropReplyRepository, never())
                .hasNonDeletedChildReplies(REPLY_ID);
    }

    @Test
    void shouldDeleteApprovedTopLevelReplyWithoutChildren() {
        DropReply reply = createReply("approved", null);
        when(dropReplyRepository.findOwnedReplyForUpdate(USER_ID, REPLY_ID))
                .thenReturn(reply);
        when(dropReplyRepository.hasNonDeletedChildReplies(REPLY_ID))
                .thenReturn(false);
        when(dropReplyRepository.softDeleteReply(USER_ID, REPLY_ID))
                .thenReturn(1);
        when(dropReplyRepository.decrementDropReplyCount(DROP_ID))
                .thenReturn(1);

        boolean deleted = dropReplyService.deleteReply(
                USER_ID,
                new DeleteReplyRequest(REPLY_ID)
        );

        assertTrue(deleted);
        verify(dropReplyRepository, never())
                .decrementChildReplyCount(PARENT_REPLY_ID);
    }

    @Test
    void shouldRejectTopLevelReplyWithChildren() {
        DropReply reply = createReply("approved", null);
        when(dropReplyRepository.findOwnedReplyForUpdate(USER_ID, REPLY_ID))
                .thenReturn(reply);
        when(dropReplyRepository.hasNonDeletedChildReplies(REPLY_ID))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> dropReplyService.deleteReply(
                        USER_ID,
                        new DeleteReplyRequest(REPLY_ID)
                )
        );

        verify(dropReplyRepository, never())
                .softDeleteReply(USER_ID, REPLY_ID);
    }

    @Test
    void shouldRejectReplyNotOwnedByUser() {
        when(dropReplyRepository.findOwnedReplyForUpdate(USER_ID, REPLY_ID))
                .thenReturn(null);

        assertThrows(
                NoSuchElementException.class,
                () -> dropReplyService.deleteReply(
                        USER_ID,
                        new DeleteReplyRequest(REPLY_ID)
                )
        );
    }

    @Test
    void shouldDeletePendingReplyWithoutUpdatingCounts() {
        DropReply reply = createReply("pending", PARENT_REPLY_ID);
        when(dropReplyRepository.findOwnedReplyForUpdate(USER_ID, REPLY_ID))
                .thenReturn(reply);
        when(dropReplyRepository.softDeleteReply(USER_ID, REPLY_ID))
                .thenReturn(1);

        boolean deleted = dropReplyService.deleteReply(
                USER_ID,
                new DeleteReplyRequest(REPLY_ID)
        );

        assertTrue(deleted);
        verify(dropReplyRepository, never()).decrementDropReplyCount(DROP_ID);
        verify(dropReplyRepository, never())
                .decrementChildReplyCount(PARENT_REPLY_ID);
    }

    private DropReply createReply(String status, Long parentReplyId) {
        return new DropReply(
                REPLY_ID,
                DROP_ID,
                USER_ID,
                parentReplyId,
                "reply/audio.m4a",
                status,
                0L,
                0L,
                0L,
                null,
                null,
                null
        );
    }
}
