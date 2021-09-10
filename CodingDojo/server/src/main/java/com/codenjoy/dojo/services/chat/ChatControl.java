package com.codenjoy.dojo.services.chat;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.web.rest.pojo.PMessage;

import java.util.List;

public interface ChatControl {

    List<PMessage> getAllRoom(Filter filter);

    List<PMessage> getAllTopic(int topicId, Filter filter);

    List<PMessage> getAllField(Filter filter);

    PMessage get(int id, String room);

    PMessage postRoom(String text, String room);

    PMessage postTopic(int topicId, String text, String room);

    PMessage postField(String text, String room);

    boolean delete(int id, String room);

    interface OnChange {

        /**
         * @param message Удаленное сообщение
         * @param playerId Игрок в том же чате, которого надо проинформировать.
         */
        void deleted(PMessage message, String playerId);

        /**
         * @param message Созданное сообщение.
         * @param playerId Игрок в том же чате, которого надо проинформировать.
         */
        void created(PMessage message, String playerId);
    }
}
