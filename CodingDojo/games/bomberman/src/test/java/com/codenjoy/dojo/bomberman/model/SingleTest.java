package com.codenjoy.dojo.bomberman.model;

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


import com.codenjoy.dojo.bomberman.services.Events;
import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.services.multiplayer.Single;
import com.codenjoy.dojo.services.printer.PrinterFactory;
import com.codenjoy.dojo.services.printer.PrinterFactoryImpl;
import com.codenjoy.dojo.services.round.RoundSettingsWrapper;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.util.LinkedList;
import java.util.List;

import static com.codenjoy.dojo.bomberman.model.BombermanTest.*;
import static com.codenjoy.dojo.services.settings.SimpleParameter.v;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SingleTest {

    public static final int SIZE = 5;
    private Walls walls = emptyWalls();
    private List<Hero> heroes = new LinkedList<>();
    private List<Game> games = new LinkedList<>();
    private List<EventListener> listeners = new LinkedList<>();
    private GameSettings settings;
    private Level level;
    private Bomberman board;
    private int bombsCount = 1;
    private Dice meatChopperDice;
    private Dice bombermanDice;
    private PrinterFactory printerFactory = new PrinterFactoryImpl();

    public void givenBoard() {
        settings = mock(GameSettings.class);

        level = mock(Level.class);
        when(level.bombsCount()).thenReturn(bombsCount);
        when(level.bombsPower()).thenReturn(1);

        bombermanDice = mock(Dice.class);

        dice(bombermanDice,  0, 0);
        heroes.add(new Hero(level, bombermanDice));
        
        dice(bombermanDice,  0, 0);
        heroes.add(new Hero(level, bombermanDice));

        OngoingStubbing<Hero> when = when(settings.getBomberman(any(Level.class)));
        for (Hero h : heroes) {
            when = when.thenReturn(h);
        }

        when(settings.getLevel()).thenReturn(level);
        when(settings.getBoardSize()).thenReturn(v(SIZE));
        when(settings.getWalls(any(Bomberman.class))).thenReturn(walls);
        RoundSettingsWrapper roundSettings = getRoundSettings();
        when(settings.getRoundSettings()).thenReturn(roundSettings);
        when(settings.killOtherBombermanScore()).thenReturn(v(200));
        when(settings.killMeatChopperScore()).thenReturn(v(100));
        when(settings.killWallScore()).thenReturn(v(10));

        board = new Bomberman(settings);

        listeners.add(mock(EventListener.class));
        listeners.add(mock(EventListener.class));

        games.add(new Single(new Player(listener(0), roundSettings.roundsEnabled()), printerFactory));
        games.add(new Single(new Player(listener(1), roundSettings.roundsEnabled()), printerFactory));

        games.forEach(g -> {
            g.on(board);
            g.newGame();
        });
    }

    private void dice(Dice dice, int... values) {
        OngoingStubbing<Integer> when = when(dice.next(anyInt()));
        for (int value : values) {
            when = when.thenReturn(value);
        }
    }

    private Walls emptyWalls() {
        Walls walls = mock(WallsImpl.class);
        when(walls.iterator()).thenReturn(new LinkedList<Wall>().iterator());
        return walls;
    }

    @Test
    public void shouldGameReturnsRealJoystick() {
        givenBoard();
        hero(0).act();
        hero(1).up();
        tick();
        hero(1).up();
        tick();
        tick();
        tick();
        tick();

        assertFalse(hero(0).isAlive());
        assertTrue(hero(1).isAlive());

        Joystick joystick1 = game(0).getJoystick();
        Joystick joystick2 = game(0).getJoystick();

        // when
        game(0).newGame();
        game(1).newGame();

        // then
        assertNotSame(joystick1, game(0).getJoystick());
        assertNotSame(joystick2, game(0).getJoystick());
    }

    private Hero hero(int index) {
        return heroes.get(index);
    }

    private Game game(int index) {
        return games.get(index);
    }

    @Test
    public void shouldGetTwoBombermansOnBoard() {
        givenBoard();

        assertSame(hero(0), game(0).getJoystick());
        assertSame(hero(1), game(1).getJoystick());

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♥   \n", game(0));

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "♥☺   \n", game(1));
    }

    @Test
    public void shouldOnlyOneListenerWorksWhenOneBombermanKillAnother() {
        givenBoard();

        hero(0).act();
        hero(0).up();
        tick();
        hero(0).up();
        tick();
        tick();
        tick();
        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "☺    \n" +
                "҉    \n" +
                "҉♣   \n", game(0));

        verify(listener(0), only()).event(Events.KILL_OTHER_BOMBERMAN);
        verify(listener(1), only()).event(Events.KILL_BOMBERMAN);
    }

    private EventListener listener(int index) {
        return listeners.get(index);
    }

    private void tick() {
        board.tick();
    }

    @Test
    public void shouldPrintOtherBombBomberman() {
        givenBoard();

        hero(0).act();
        hero(0).up();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☻♥   \n", game(0));

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "♠☺   \n", game(1));
    }

    @Test
    public void shouldBombermanCantGoToAnotherBomberman() {
        givenBoard();

        hero(0).right();
        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♥   \n", game(0));
    }

    private void assertBoard(String board, Game game) {
        assertEquals(board, game.getBoardAsString());
    }

    // бомбермен может идти на митчопера, при этом он умирает
    @Test
    public void shouldKllOtherBombermanWhenBombermanGoToMeatChopper() {
        walls = new MeatChopperAt(2, 0, new WallsImpl());
        givenBoard();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♥&  \n", game(0));

        hero(1).right();
        tick();
        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺ ♣  \n", game(0));

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "♥ Ѡ  \n", game(1));

        verifyNoMoreInteractions(listener(0));
        verify(listener(1), only()).event(Events.KILL_BOMBERMAN);
    }

    // если митчопер убил другого бомбермена, как это на моей доске отобразится? Хочу видеть трупик
    @Test
    public void shouldKllOtherBombermanWhenMeatChopperGoToIt() {
        meatChopperAt(2, 0);
        givenBoard();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♥&  \n", game(0));

        dice(meatChopperDice, Direction.LEFT.value());
        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♣   \n", game(0));

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "♥Ѡ   \n", game(1));

        verifyNoMoreInteractions(listener(0));
        verify(listener(1), only()).event(Events.KILL_BOMBERMAN);
    }

    // А что если бомбермен идет на митчопера а тот идет на встречу к нему - бомбермен проскочит или умрет? должен умереть!
    @Test
    public void shouldKllOtherBombermanWhenMeatChopperAndBombermanMoves() {
        meatChopperAt(2, 0);
        givenBoard();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♥&  \n", game(0));

        dice(meatChopperDice, Direction.LEFT.value());
        hero(1).right();
        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺&♣  \n", game(0));

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "♥&Ѡ  \n", game(1));

        verifyNoMoreInteractions(listener(0));
        verify(listener(1), only()).event(Events.KILL_BOMBERMAN);
    }

    private void meatChopperAt(int x, int y) {
        meatChopperDice = mock(Dice.class);
        dice(meatChopperDice, x, y);
        Field temp = mock(Field.class);
        when(temp.size()).thenReturn(SIZE);
        MeatChoppers meatchoppers = new MeatChoppers(new WallsImpl(), temp, v(1), meatChopperDice);
        meatchoppers.regenerate();
        walls = meatchoppers;
    }

    //  бомбермены не могут ходить по бомбам ни по своим ни по чужим
    @Test
    public void shouldBombermanCantGoToBombFromAnotherBomberman() {
        givenBoard();

        hero(1).act();
        hero(1).right();
        tick();
        hero(1).right();
        hero(0).right();
        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺3 ♥ \n", game(0));

        hero(1).left();
        tick();
        hero(1).left();
        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺1♥  \n", game(0));
    }

    @Test
    public void shouldBombKillAllBomberman() {
        shouldBombermanCantGoToBombFromAnotherBomberman();

        tick();
        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                " ҉   \n" +
                "Ѡ҉♣  \n", game(0));

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                " ҉   \n" +
                "♣҉Ѡ  \n", game(1));
    }

    @Test
    public void shouldNewGamesWhenKillAll() {
        shouldBombKillAllBomberman();
        when(settings.getBomberman(any(Level.class))).thenReturn(new Hero(level, bombermanDice), new Hero(level, bombermanDice));

        game(0).newGame();
        game(1).newGame();
        tick();
        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♥   \n", game(0));

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "♥☺   \n", game(1));
    }

    // на поле можно чтобы каждый поставил то количество бомб которое ему позволено и не более того
    @Test
    public void shouldTwoBombsOnBoard() {
        bombsCount = 1;

        givenBoard();

        hero(0).act();
        hero(0).up();

        hero(1).act();
        hero(1).up();

        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♥   \n" +
                "44   \n", game(0));

        hero(0).act();
        hero(0).up();

        hero(1).act();
        hero(1).up();

        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "☺♥   \n" +
                "     \n" +
                "33   \n", game(0));

    }

    @Test
    public void shouldFourBombsOnBoard() {
        bombsCount = 2;

        givenBoard();

        hero(0).act();
        hero(0).up();

        hero(1).act();
        hero(1).up();

        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "☺♥   \n" +
                "44   \n", game(0));

        hero(0).act();
        hero(0).up();

        hero(1).act();
        hero(1).up();

        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "☺♥   \n" +
                "44   \n" +
                "33   \n", game(0));

        hero(0).act();
        hero(0).up();

        hero(1).act();
        hero(1).up();

        tick();

        assertBoard(
                "     \n" +
                "☺♥   \n" +
                "     \n" +
                "33   \n" +
                "22   \n", game(0));

    }

    @Test
    public void shouldFourBombsOnBoard_checkTwoBombsPerBomberman() {
        bombsCount = 2;

        givenBoard();

        hero(0).act();
        hero(0).up();

        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                "☺    \n" +
                "4♥   \n", game(0));

        hero(0).act();
        hero(0).up();

        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "☺    \n" +
                "4    \n" +
                "3♥   \n", game(0));

        hero(0).act();
        hero(0).up();

        tick();

        assertBoard(
                "     \n" +
                "☺    \n" +
                "     \n" +
                "3    \n" +
                "2♥   \n", game(0));
    }

    @Test
    public void shouldFireEventWhenKillWallOnlyForOneBomberman() {
        walls = new DestroyWallAt(0, 0, new WallsImpl());
        givenBoard();

        hero(0).act();
        hero(0).right();
        hero(1).up();
        tick();
        hero(0).right();
        hero(1).up();
        tick();
        hero(0).right();
        hero(1).up();
        tick();
        tick();
        tick();

        assertBoard(
                " ♥   \n" +
                "     \n" +
                "     \n" +
                " ҉   \n" +
                "H҉҉ ☺\n", game(0));

        verify(listener(0)).event(Events.KILL_DESTROY_WALL);
        verifyNoMoreInteractions(listener(1));
    }

    @Test
    public void shouldFireEventWhenKillMeatChopper() {
        walls = new MeatChopperAt(0, 0, new WallsImpl());
        givenBoard();

        hero(0).act();
        hero(0).right();
        hero(1).up();
        tick();
        hero(0).right();
        hero(1).up();
        tick();
        hero(0).right();
        hero(1).up();
        tick();
        tick();
        tick();

        assertBoard(
                " ♥   \n" +
                "     \n" +
                "     \n" +
                " ҉   \n" +
                "x҉҉ ☺\n", game(0));

        verify(listener(0)).event(Events.KILL_MEAT_CHOPPER);
        verifyNoMoreInteractions(listener(1));
    }

    @Test
    public void bug() {
        walls = new DestroyWallAt(0, 0, new MeatChopperAt(1, 0, new MeatChopperAt(2, 0, new WallsImpl())));
        givenBoard();

        assertBoard(
                "     \n" +
                "     \n" +
                "     \n" +
                " ☺♥  \n" +
                "#&&  \n", game(0));

        hero(0).act();
        hero(0).up();
        hero(1).act();
        hero(1).up();
        tick();
        hero(0).left();
        hero(1).right();
        tick();
        tick();
        tick();
        tick();

        assertBoard(
                "     \n" +
                "     \n" +
                "☺҉҉♥ \n" +
                "҉҉҉҉ \n" +
                "#xx  \n", game(0));

        verify(listener(0), only()).event(Events.KILL_MEAT_CHOPPER);
        verify(listener(1), only()).event(Events.KILL_MEAT_CHOPPER);
    }
}
