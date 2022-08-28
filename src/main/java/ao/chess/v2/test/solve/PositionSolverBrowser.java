package ao.chess.v2.test.solve;


import ao.chess.v2.engine.heuristic.learn.PgnGenerator;
import ao.chess.v2.engine.heuristic.learn.PgnParser;
import ao.chess.v2.engine.neuro.rollout.RolloutNode;
import ao.chess.v2.engine.neuro.rollout.store.FileRolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.RolloutStore;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import com.google.common.primitives.Ints;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class PositionSolverBrowser {
    public static void main(String[] args) throws Exception {
        State nextState = State.initial();

        String history = "" +
//                "1.e4 c5 2.Nf3 d6";
//                "1.c4 e5 2.Nc3 Nf6";
//                "1.c4 c5 2.Nf3 Nc6 3.Nc3 e5 4.g3 g6 5.a3 Bg7 6.Bg2 Nge7 7.O-O d6 8.Rb1 O-O 9.d3 f5 10.Bg5";
//                "1.c4 c5 2.Nf3 g6 3.e4 Bg7 4.d4 cxd4 5.Nxd4 Nc6 6.Nc2 Qb6 7.Nc3 Bxc3 8.bxc3 Qa5 9.f3 b6 10.Be2 Nf6 " +
//                    "11.O-O O-O";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.e3 Bf5 5.Nc3";
//                "1.d4 d5 2.Nf3 Nf6 3.c4 dxc4 4.e3 e6 5.Bxc4 c5 6.O-O a6 7.a4 cxd4 8.exd4 Nc6 9.Nc3 Bb4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 a6 5.Bxc4 Nf6 6.O-O Nbd7";
//                "1.d4 c6 2.e4 d5 3.e5 Bf5 4.c4 e6 5.Nc3 Ne7";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5";
//                "1.d4 c6 2.c4 d5 3.Nf3 Nf6 4.Nc3 e6 5.e3 Nbd7 6.Qc2 Bd6 7.Bd3 O-O 8.O-O";
//                "1.d4 c6 2.Nf3 Nf6 3.c4 d5 4.e3 Bf5 5.Nc3 a6 6.Bd2 e6 7.Nh4 Bg4 8.Qb3";
//                "1.d4 c6 2.Nf3 Nf6 3.c4 d5 4.e3 a6 5.Nc3 Bf5 6.Bd3 Bxd3 7.Qxd3 e6 8.O-O Be7 9.e4 O-O 10.Bf4 dxe4 " +
//                    "11.Nxe4";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.e3 Bf5 5.Nc3 a6 6.Bd3 Bg6 7.Bxg6 hxg6 8.O-O";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.e3 a6 5.Nbd2 Bf5 6.b4 Nbd7 7.c5 e6 8.a4 Rg8";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 O-O"; // x
//                "1.d4 d5 2.c4 dxc4 3.e3 a6 4.Bxc4 Nf6 5.Nf3 e6 6.O-O c5";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Nf3 Nf6 5.Bxc4 c5 6.O-O a6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Nf3 Nf6 5.Bxc4 c5 6.O-O a6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.Qe2 Nc6";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Bxc4 Nf6 5.Nf3 c5 6.O-O a6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Bxc4 c5 5.Nf3 Nf6 6.O-O a6 7.a4 cxd4";
//                "1.d4 d5 2.c4 dxc4 3.e3 a6 4.Bxc4 c5 5.Nf3 e6 6.O-O Nf6 7.a4";
//                "1.d4 d5 2.c4 dxc4 3.e3 a6 4.Bxc4 e6 5.Nf3 c5 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.Qb3 b5 16.Rfd1 Qe8 17.axb5 Be6 18.Qd3 Bxa2 " +
//                    "19.Rxa2 Qxb5 20.Qxb5 axb5 21.Rxa8 Rxa8 22.g3 h5 23.h3 g6";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Bxc4 a6 5.Nf3 c5 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.Qb3 b5 16.Rfd1 Qe8 17.axb5 Be6 18.Qd3 Bxa2 " +
//                    "19.Rxa2 Qxb5 20.Qxb5 axb5 21.Rxa8 Rxa8 22.g3 h5 23.h3";
//                "1.d4 d5 2.c4 dxc4 3.e3 Nf6 4.Bxc4 a6 5.Nf3 e6 6.O-O c5 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5";
//                "1.d4 c6 2.Nf3 Nf6 3.c4 d5 4.e3 Bf5 5.Nc3 a6";
//                "1.d4 c6 2.c4 Nf6 3.Nf3 d5 4.e3 Bf5 5.Nc3 a6 6.Be2 e6 7.O-O h6 8.Bd3 Bxd3 9.Qxd3 Bb4 10.a3 Bxc3 " +
//                    "11.Qxc3 O-O 12.b3 Nbd7";//                "1. e4";
//                "1.c4 e5 2.g3 Nc6";
//                "1.c4 c5 2.Nf3 Nf6 3.Nc3 e5";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Bb4 5.Nd5 e4 6.Nh4 O-O 7.Bg2 Re8 8.O-O d6 9.b3 g5 10.Bb2 Nd5 " +
//                    "11.cd5 Ne5 12.f4 gh4 13.fe5 de5 14.e3 Qg5 15.gh4 Qh4 16.Qe1";
//                "1.c4 e5 2.g3 Nc6 3.Nc3 f5 4.Nf3 Nf6 5.Bg2 g6 6.Rb1 Bg7 7.O-O a5 8.d3 d6 9.a3";
//                "1.c4 e5 2.g3 Nc6 3.Nc3 f5 4.Nf3 Nf6 5.Bg2 g6 6.Rb1 Bg7 7.O-O O-O 8.d3 d6 9.b4 h6 10.b5 Ne7 " +
//                    "11.Qb3 Nd7 12.Ba3 Kh7 13.Bb4 a6 14.Rfc1 a5 15.Ba3 Rb8 16.b6 c5 17.e3 Nc6 18.Nb5 Qe7 " +
//                    "19.Bb2 a4 20.Qxa4 Nxb6 21.Qc2 Nd7 22.a3 Nf6 23.Ba1";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Be7 5.Bg2 O-O 6.O-O Re8";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Be7 5.Bg2 h6 6.e4 O-O";
//                "1.d4 Nf6 2.c4";
//                "1.c4 e5 2.Nc3 Nf6";
//                "1. e3";
//                "1. g4";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 dxc4 5.Bg2";
//                "1.d4 Nf6 2.c4 e6 3.Nc3 Bb4 4.Nf3 O-O 5.Bg5";
//                "1.d4 Nf6 2.c4 c6 4.Nf3 d5 5.Nc3 e6 6.e3 Nbd7";
//                "1.Nf3 d5 2.d4 Nf6 3.c4 e6 4.Nc3";
//                "1.Nf3 Nf6 2.c4";
//                "1.Nf3 Nc6 2.e4 e5 3.d4 exd4 4.Nxd4 Nxd4";
//                "1.d4 d5 2.c4 dxc4 3.e3 Nf6 4.Bxc4 c5 5.Nf3 e6";
//                "1.d4 d5 2.Nf3 Nf6 3.c4 dxc4 4.e3 a6 5.Bxc4 b5 6.Bd3 Nbd7 a4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 a6 5.Bxc4 Nf6 6.O-O c5 7.b3";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.b3 Nbd7 8.Bb2 Be7 9.dxc5 Bxc5 10.Be2 b6";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 cxd4 8.Nxd4 Bd6 9.Nd2 O-O 10.b3 e5";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 Nbd7 8.Nc3 b5 9.d5 exd5 10.Nxd5 Bb7 " +
//                    "11.Nxf6 Qxf6 12.a4 b4 13.e4 Be7";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 b6 8.Nc3 cxd4 9.Qxd4 Qxd4 10.Nxd4 Bb7";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 b6 8.Nc3 cxd4 9.Nxd4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 cxd4 8.Nxd4 Be7 9.a3 O-O 10.b4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 cxd4 8.Nxd4 Be7 9.b3 Bd7 10.Nd2 Nc6";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 Nbd7";
//                "1.d4 d5 2.Nf3 Nf6 3.c4 dxc4 4.e3 e6 5.Bxc4 c5 6.O-O a6 7.Be2 cxd4 8.Nxd4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.Be2 Nbd7 8.Nc3 b5 9.d5 exd5 10.Nxd5 Bb7 " +
//                    "11.Nxf6 Qxf6 12.a4 b4 13.e4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Bc4 Qxd1 15.Rfxd1 Bf5 16.Bb3 Rfd8 17.Ng5 Bg6 18.Bb6 Rxd1 " +
//                    "19.Rxd1 Rc8";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.a5 Bb4 16.Bb6 Qe7 17.Rc1 Rac8 18.h4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.Qb3 b5 16.Rfd1 Qe8 17.axb5 Be6 18.Qd3 Bxa2 " +
//                    "19.Rxa2 Qxb5 20.Qxb5 axb5 21.Rxa8 Rxa8 22.Kf1 h5 23.Ke2";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.a5 Nf6 14.Ba2 Qxd1 15.Rxd1 h6 16.Bd2 Rd8 17.Re1 Kf8 18.Bc3";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Qxd1 15.Rfxd1 Bd7 16.a5 Rac8 17.Bd4 Rfd8 18.Bb6 Re8 " +
//                    "19.Ng5 Rf8 20.h3 Bb5 21.Rd4 Rc2 22.Re1 Rxb2 23.Rxe7 Rxa2 24.Rxb7 h6 25.Nf3 Nh5";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 Nf6 6.O-O a6 7.Be2 Nbd7";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 Nf6 6.O-O a6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.Qb3 Be4 16.Rfd1";

//                "1.d4 d5 2.c4 e6 3.Nc3 Nf6";
//                "1.d4 e6 2.c4 d5 3.Nc3 Nf6";
//                "1.d4 d5 2.c4 e6 3.Nc3 c5"; //
//                "1.d4 c5 2.d5 g6 3.e4";
//                "1.d4 c5 2.d5 f5 3.h4";
//                "1.d4 c5 2.d5 Nf6 3.c4 b5";
//                "1.c4 c5 2.Nf3 Nf6 3.Nc3 d5";
//                "1.c4 c5 2.Nf3 Nf6 3.Nc3 d5";
//                "1.c4 c5 2.Nf3 Nc6 3.d4 cxd4 4.Nxd4 Nf6 5.Nc3";
//                "1.e4 e5 2.Nf3 Nc6";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.f4";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Qc2";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nd2 b4 5.e4";
//                "1.d4 d5 2.c4 3.e6 cxd5 5.exd5 Nc3";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nd2 g6 5.e4";
//                "1.d4 Nf6 2.c4 e5 3.dxe5 Ng4 4.Bf4 g5 5.Bg3 Bg7 6.Nf3 Nc6 7.Nc3 Ngxe5 8.Nxe5 Nxe5 9.e3 d6 10.Be2 Be6";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nd2 bxc4 5.e4 c3 6.bxc3 g6 7.Ngf3 Bg7 8.Bd3 O-O 9.O-O d6 10.Rb1 Nbd7";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.b6 Qxb6 6.Nc3 d6 7.e4 g6 8.Nf3 Bg7 9.Be2 O-O 10.O-O Bg4";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.b6 a5 6.Nc3"; // x
//                "1.c4 c5 2.Nf3 g6 3.d4 cxd4 4.Nxd4"; // x
//                "1.Nf3 d5 2.d4 c5 3.c4 e6"; // x
//                "1.e4 c5 2.Nf3 d6 3.d4"; // x

//                "1.e4 e5 2.Nf3 Nc6 3.Bb5";
//                "1.e4 c5 2.Nf3 Nc6 3.c3 Nf6";
//                "1.e4 Nc6 2.d4 d5 3.exd5";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6";
//                "1.c4 c5 2.Nf3 Nf6 3.Nc3 d5";
//                "1.c4 c5 2.Nf3 Nf6 3.Nc3 Nc6 4.e3";
//                "1.c4 c5 2.Nc3 Nc6 3.Nf3 e6";
//                "1.c4 c5 2.Nf3 g6 3.e3 Nf6 4.Nc3";
//                "1.c4 Nf6 2.Nc3 c5 3.Nf3 Nc6";
//                "1.c4 Nf6 2.Nf3 c5 3.Nc3 Nc6";
//                "1.c4 c5 2.Nf3 Nc6 3.e3 Nf6 4.h3";
//                "1.c4 c5 2.Nf3 g6 3.Nc3 Bg7 4.e3 Nf6";
//                "1.Nf3 d5 2.d4 c5 3.c4 cxd4 4.cxd5 Nf6";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.bxa6 d6 6.Nc3";
//                "1.Nf3 Nf6 2.c4 c5 3.g3 Nc6 4.Bg2 d5";
//                "1.Nf3 c5 2.c4 g6 3.Nc3 Bg7";
//                "1.c4 c5 2.Nc3 g6 3.g3 Bg7 4.Bg2 Nc6";
//                "1.c4 c5 2.Nc3 Nf6 3.Nf3 g6 4.g3 Nc6";
//                "1.c4 c5 2.Nf3 g6 3.Nc3 Bg7 4.h3 Nf6";
//                "1.Nf3 Nf6 2.c4 c5 3.e3 g6 4.Nc3 Bg7";
//                "1.Nf3 Nf6 2.c4 c5 3.Nc3 Nc6 4.e3 e6 5.d4 d5";
//                "1.Nf3 Nf6 2.c4 c5 3.Nc3 g6 4.e3 Bg7 5.d4";
//                "1.Nf3 Nf6 2.c4 c5 3.Nc3 g6 4.g3 Nc6 5.Bg2 Bg7";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.bxa6 g6 6.a7 Ra7 7.Nc3";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 b4 5.Nbd2 a5";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 g6 5.cxb5 a6 6.b6";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.h3 b4 5.Nbd2 d6 6.e4";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.h3 bxc4 5.Nc3 Ba6 6.Qa4";
//                "1.d4 Nf6 2.c4 c5 3.dxc5 e6 4.g3 Bxc5";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.b6 Qxb6 6.Nc3 d6 7.h3";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.b6 g6 6.Nc3 Qxb6";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.bxa6 Bxa6 6.Nc3 d6 7.Qc2";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.b6 a5 6.Nc3 g6 7.e4 d6";
//                "1.d4 Nf6 2.Nf3 c5 3.d5 b5 4.c4 g6 5.cxb5";
//                "1.d4 Nf6 2.Nf3 c5 3.d5 b5 4.e4 Nxe4 5.Bd3";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 g6 5.Nbd2 bxc4 6.e4 c3 7.bxc3 Bg7 8.Qc2";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 g6 5.Nbd2 bxc4 6.e4 Bg7 7.Bxc4 O-O 8.O-O d6 9.Re1";
//                "1.d4 g6 2.c4 c5 3.d5 b5";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.cxb5 a6 5.b6 e6 6.e4 Nxe4 7.Nc3 Nxc3 8.bxc3 Qxb6";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 bxc4 5.Nc3 d6 6.e4 Nbd7 7.Bxc4 g6";
//                "1.d4 d6 2.Nc3 Nf6 3.e4";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 d6 5.cxb5 a6 6.b6 Nbd7 7.Nc3";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 d6 5.Nbd2 bxc4 6.e4 Nbd7 7.Bxc4 g6 8.b3";
//                "1.d4 Nf6 2.c4 e6 3.Nc3 c5 4.d5";
//                "1.d4 Nf6 2.c4 a6 3.Nc3 c5";
//                "1.d4 Nf6 2.c4 a6 3.Nf3 c5 4.dxc5 e6";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 g6 5.Nbd2 Bg7 6.e4 bxc4 7.Bxc4 O-O 8.O-O d6 9.Rb1 Ba6 10.b3 Bxc4 11.bxc4 Nbd7 12.h3 Qc7 13.Qc2 Rfb8 13.Rb3 Nh5 14.Ra3 a5 15.Ne1"; // x
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 g6 5.Nbd2 Bg7 6.e4 bxc4 7.Bxc4 O-O 8.O-O d6 9.Rb1 Ba6 10.b3 Bxc4 11.bxc4 Nbd7 12.h3 Rb8 13.Rb3 Rxb3 14.axb3";
//                "1.d4 Nf6 2.c4 c5 3.d5 b5 4.Nf3 g6 5.Nbd2 Bg7 6.e4 O-O 7.cxb5 a6 8.a4 axb5 9.Bxb5 Ba6 10.Ra3 d6 11.Qb3 Nbd7 12.Bxa6 Rxa6";

//                "1.e4 c5 2.Nc3 g6 3.Nf3 Bg7";

//                "1.Nf3 Nf6 2.c4 e6 3.g3 Be7 4.Bg2 O-O 5.O-O d5 7.d4 dxc4 8.Qc2";
//                "1.Nf3 Nf6 2.c4 e6 3.g3 d5 4.Bg2 Be7 5.O-O O-O 6.d4 Nbd7 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2"; // x
//                "1.Nf3 Nf6 2.c4 e6 3.g3 d5 4.d4 Be7 5.Bg2 Nbd7 6.Qc2 h6";
//                "1.d4 Nf6 2.c4 e6 3.g3 d5 4.Nf3 Be7 5.Bg2 O-O 6.Nc3";
//                "1.d4 Nf6 2.c4 e6 3.Nc3 d5 4.cxd5";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.a3";
//                "1.c4 e5 2.g3 Nf6 3.Bg2 c6 4.Nf3 e4 5.Nd4";
//                "1.c4 e5 2.g3 Nf6 3.Bg2 Nc6 4.Nc3 h6";
//                "1.c4 Nf6 2.Nf3 e6 3.g3 d5 4.Bg2 Be7 5.O-O O-O 6.d4 Nbd7 7.Qc2 c6"; // x
//                "1.c4 Nf6 2.d e6 3.e3 Be7 4.d5";
//                "1.c4 Nf6 2.Nc3 e5 3.Nf3 Nc6 4.g3 Bb4 5.Nd5 e4 6.Nh4 O-O 7.Bg2 d6 8.Nxb4 Nxb4 9.a3 Na6 10.d3 exd3 11.Qxd3 Nxc5 12.Qxc2 a5";
//                "1.c4 Nf6 2.g3 e6 3.Bg2 d5 4.Nf3 Be7 5.O-O O-O 6.b3 d4 7.e3 c5";
//                "1.c4 Nf6 2.Nc3 e5 3.Nf3 Nc6 4.g3 Bb4 5.Nd5 e4 6.Nh4 O-O 7.Bg2 d6 8.Nxb4 Nxb4 9.a3 Nc6 10.d3 d5 11.cxd5 Qxd5 12.b4";
//                "1.c4 c6 2.Nf3 d5 3.e3 Nf6";
//                "1.d4 Nf6 2.Nf3 d5 3.c4 e6 4.Nc3 c5 5.cxd5";
//                "1.d4 Nf6 2.c4 e6 3.Nc3 d5 4.cxd5 exd5 5.Bg5 c6 6.Qc2 Be7 7.e3 O-O 8.Bd3 h6 9.Bh4 c5 10.Nge2 Nc6 11.O-O cxd4";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 O-O 6.O-O dxc4 7.Qc2 a6 8.a4 Bd7 9.Qxc4 Bc6 10.Bf4 Nbd7 11.Nc3 a5 12.Qd3";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.Rac1 Ba8 20.h4 a5 21.Nd4 Qd7 22.Qb1 b5 23.b4 axb4 24.axb4 N5a6 25.cxb5 Nxb5 26.Nxb5 Qxb5 " +
//                    "27.Bf1 Qb7 28.b5 d4 29.Qe4 Qb8 30.Rxc8 Rxc8 31.Qxd4 Rd8 32.Qc4 Nb4 33.Bd4 Nc2 34.Qxc2 Rxd4 " +
//                    "35.Ra1 Qb7 36.Qc6 Qxc6 37.bxc6 Bxc6 38.Nc4 Bf8 39.Rc1"; // x
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 a5 21.Nd4 Ba8 22.h3 Qe8 23.Re1 Qd8 24.Rad1 axb4 25.axb4 Nb8 26.Nb5 Nba6 " +
//                    "27.Na7 Rb8 28.b5 Nc5 29.Qc2 Qe8 30.Bd4 Rd8 31.Nf1 Rd7 32.Nc6 Bxc6 33.bxc6 Rd8 34.Ne3 Qxc6"; // x
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 a5 21.Nd4 Ba8 22.h3 Qe8 23.Re1 Qd8 24.Rad1 axb4 25.axb4 Re8 26.h4 Bf8 " +
//                    "27.Ba3 Bb7"; // x
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                "19.b4 Nd7 20.Qb3 Ba8 21.Nd4 a6 22.h4 Re8 23.f4 Bf8 24.Rac1 a5 25.b5"; // x
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 Ba8 21.Nd4 a6 22.h4 dxc4 23.Nxc4 Bxg2 24.Kxg2 b5 25.Nd6 Bxd6 26.exd6 Ne8 " +
//                    "27.a4 Nb6 28.axb5 axb5 29.Nxb5 Qd7 30.Ra7 Qxb5 31.d7 Nxd7 32.Raxd7 Rc4 33.Qd3 Rxb4 34.Ba3 Nf6 " +
//                    "35.Ra7 Qxd3 36.Rxd3 Rbb8 37.Bxf8 Rxf8 38.Rb3 g6 39.Rbb7 Nd5 40.Kf3 Kg7 41.Ke4 Kf6 42.Ra3 Kg7 " +
//                    "43.Ra6 Re8 44.Raa7 Rf8";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 Ba8 21.Nd4 a6 22.h4 dxc4 23.Nxc4 Bxg2 24.Kxg2 b5 25.Nd6 Bxd6 26.exd6 Ne8 " +
//                    "27.a4 Nb6 28.axb5 axb5 29.Nxb5 Qd7 30.Ra7 Qxb5 31.d7 Nxd7 32.Raxd7 Rc4 33.Qd3 Rxb4 34.Ba3 Nf6 " +
//                    "35.Ra7 Qxd3 36.Rxd3 Rbb8 37.Bxf8 Rxf8 38.Rb3 g6 39.Rbb7 Ng4 40.Kf3 h5 41.Kg2 Nf6 42.f3 Nd5"; // x
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 a5 21.Nd4 Ba8 22.h3 Nxe5 23.Nxe6 fxe6 24.Bxe5 Qe8 25.Qe3 dxc4 26.Bxa8 Nxa8 " +
//                    "27.Qe2 Nc7 28.Nxc4 Nd5 29.bxa5 bxa5 30.Rd4 Qc6 31.Bd6 Bf6 32.Qxe6 Rf7 33.Ne5 Bxe5 34.Qxe5 Nc3 " +
//                    "35.Re1 Nb5";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 a5 21.Nd4 Ba8 22.h3 Nxe5 23.Nxe6 fxe6 24.Bxe5 Rf5 25.f4 Qf8 26.bxa5 bxa5 " +
//                    "27.Qa4 h5 28.h4 g5 29.hxg5 h4 30.gxh4 Bc5 31.Kh1";
                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
                    "19.b4 Nd7 20.Qb3 a5 21.Nd4 Ba8 22.h3 Nxe5 23.Nxe6 fxe6 24.Bxe5 Rf5 25.Qe3 Rxe5 26.Qxe5 Bf6 " +
                    "27.Qe2 Bxa1 28.Rxa1 dxc4 29.Nxc4 Bxg2 30.Kxg2 axb4 31.axb4";
//                "1.d4 Nf6 2.Nf3 e6 3.c4 d5 4.g3 Be7 5.Bg2 O-O"; // z
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.Bb2 cxd4 14.Rac1 b5 15.Bf1 dxc4 16.bxc4 Nc7 17.Bxd4 Nd5 18.Qb3 bxc4 " +
//                    "19.Nxc4 Rc7 20.Nfd2 Bc5"; // y
//                "1.d4 Nf6 2.c4 e6 3.e3 Be7 4.Nc3 d5";
//                "1.e4 c5 2.Nf3 d6 3.Nc3 Nf6 4.d4 cxd4 5.Nxd4 a6 6.h3 b5 7.g4";
//                "1.e4 c5 2.Nf3 d6 3.Nc3 e5 4.Bc4 Be7 5.d3 Nf6 6.O-O O-O";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.cxd5 exd5 5.Nc3 c6 6.Qc2 g6 7.Bg5";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 O-O 6.O-O dxc4 7.a4 c5 8.dxc5";
//                "1.d4 Nf6 2.c4 e6 3.Nc3 d5 4.cxd5 exd5 5.Bg5 c6 6.Qc2 Be7 7.e3 Nbd7 8.h3 O-O 9.Bd3 Re8";
//                "1.d4 Nf6 2.c4 e6 3.a3 c5 4.e3 d5 5.Nf3";
//                "1.d4 d5 2.c4 c6 3.Nf3 Nf6 4.e3 Bf5 5.Nc3 e6 6.Nh4 Bg6 7.Nxg6 hxg6";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.e3 Nbd7 5.Nbd2 e6 6.b3 Be7 7.Bb2 O-O"; // x
//                "1.d4 d5 2.c4 c6 3.Nf3 Nf6 4.Nc3 e6 5.e3 Nbd7 6.Bd3 dxc4 7.Bxc4 b5 8.Bd3";
//                "1.d4 d5 2.c4 c6 3.Nf3 Nf6 4.Nc3 e6 5.e3 Nbd7";
//                "1.d4 d5 2.c4 dxc4 3.e4 Nf6 4.e5 Nfd7 5.Bxc4 Nb6";
//                "1.d4 d5 2.c4 dxc4 3.Nf3 a6 4.e3 Nf6 5.Bc4 e6 6.O-O";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.Nc3 e6 5.e3 Nbd7 6.Bd3 dxc4 6.Bxc4 b5";
//                "1.e4 c5 2.Nf3 d6 3.d4 cxd4 4.Nxd4 Nf6 5.Nc3 a6 6.Bg5";
//                "1.e4 e5 2.Nf3 Nc6 3.Bb5 a6 4.Ba4 Nf6 5.O-O Be7 6.Re1 b5 7.Bb3 d6 8.c3 O-O 9.h3"; // x
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Bb4 5.Bg2 O-O 6.O-O e4";

        List<State> moveHistories = PgnParser.parse(history);

        RolloutStore store = new FileRolloutStore(
                PositionSolver.treeDir.resolve("root.bin"),
                null);
//                Paths.get("lookup/tree/root-1.bin"));

        System.out.println("As of: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("PGN: " + history);

        long nextIndex = RolloutStore.rootIndex;
        printDetails(nextIndex, nextState, store);

        for (int i = 1; i < moveHistories.size(); i++) {
            State moveState = moveHistories.get(i);
            int move = PgnGenerator.findMove(nextState, moveState);
            int moveIndex = Ints.indexOf(nextState.legalMoves(), move);

            System.out.println(i + "\t" + Move.toInputNotation(move) + "\t" + Move.toString(move));

            nextIndex = store.getChildIndex(nextIndex, moveIndex);
            nextState = moveState;

            printDetails(nextIndex, nextState, store);
        }

        store.close();
    }


    private static void printDetails(long index, State state, RolloutStore store) {
        String detail = new RolloutNode(index).toString(state, store);
        System.out.println(detail);
    }
}
