(
s.waitForBoot {
	JXSynthDef('/synthA', {
		var a = JXOscSink.kr('/inA', 220, \freq);
		var b = JXOscSink.kr('/inB', 440, \freq);

		(a + b) / 2    |> JXOscSrc.kr('/outA', _, \freq);
		LFNoise2.kr(1) |> JXOscSrc.kr('/outB', _, \bipolar);
	});

	~mapMkA = JXOscMapMk({ |srcs|
		JXOscMap((
			'/synthA/inA' : 0.2,
			'/synthA/inB' : 0.3,
		))
	});

	~mapMkB = JXOscMapMk({ |srcs|
		var dtrig = Dust.kr(0.1);
		JXOscMap((
			'/synthA/inA' : Dwhite(0.2, 0.8) |> Demand.kr(dtrig, 0, _),
			'/synthA/inB' : srcs['/synthA/outB'],
		))
	});

	JXOscMapperSynth({
		var src = JXOscStore.getMapSources();

		var a = ~mapMkA.makeMap(src);
		var b = ~mapMkB.makeMap(src);

		var lerp = JXOscMapLinSelectX(MouseX.kr(0, 1), a, b);

		JXOscMapOutput.kr(lerp);
	});

	JXOscRelay.init(sendingRate: 120);

	fork { loop {
		var v = JXOscStore.getByOscAddr('/synthA/outA').bus.getSynchronous;
		\freq.asSpec.map(v).postln;
		0.125.wait;
	}}
}
)












///

(
s.waitForBoot {
	JXSynthDef('/synthA', {
		var a = JXOscSink.kr('/inA', 220, \freq);
		var b = JXOscSink.kr('/inB', 440, \freq);
		var c = JXOscSink.kr('/inC', 0, [0.001, 0.5, \exp]);

		['/outA', (a + b) / 2]      *|> JXOscSrc.kr(_, _, \freq);
		['/outB', LFNoise2.kr(1)]   *|> JXOscSrc.kr(_, _, \bipolar);
		['/outC', LFDNoise3.kr(c)]   *|> JXOscSrc.kr(_, _, \bipolar);
	});

	~mapMkA = JXOscMapMk({ |srcs|
		JXOscMap((
			'/synthA/inA' : srcs['/synthA/outA'],
			'/synthA/inB' : srcs['/synthA/outB'],
		))
	});

	~mapMkB = JXOscMapMk({ |srcs|
		JXOscMap((
			'/synthA/inA' : srcs['/synthA/outB'],
		))
	});

	~mapMkC = JXOscMapMk({ |srcs|
		JXOscMap((
			'/synthA/inA' : 0.8,
			'/synthA/inB' : srcs['/synthA/outA'] + srcs['/synthA/outC'],
		))
	});

	~mapMkD = JXOscMapMk({ |srcs|
		var a = ~mapMkA.makeMap(srcs);
		var b = ~mapMkB.makeMap(srcs);
		var c = ~mapMkC.makeMap(srcs);

		JXOscMapLinSelectX(LFNoise2.kr(0.01).range(0, 2), a, b, c);
	});

	JXOscMapperSynth({
		var src = JXOscStore.getMapSources();
		var m = ~mapMkD.makeMap(src) ++ JXOscMap(('/synthA/inC': MouseX.kr));
		JXOscMapOutput.kr(m);
	});

	JXOscRelay.init(sendingRate: 120);

	fork { loop {
		var get = {|n| JXOscStore.getByOscAddr(n).bus.getSynchronous.round(0.01) };
		var a = get.('/synthA/outA');
		var b = get.('/synthA/outB');
		var c = get.('/synthA/outC');
		var fmt = _.asStringPrec(0.001);

		"a: %, b: %, c: %".format(a |> fmt, b |> fmt, c |> fmt).postln;

		0.125.wait;
	}}
}
)




/// What would this look like in Max? Or any other language without keyvalue pairs?












