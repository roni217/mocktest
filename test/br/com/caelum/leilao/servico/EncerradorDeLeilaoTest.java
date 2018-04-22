package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.ICarteiro;
import br.com.caelum.leilao.dominio.IRepositorioDeLeiloes;
import br.com.caelum.leilao.dominio.Leilao;

public class EncerradorDeLeilaoTest {
	// atLeastOnce()
	// atLeast(numero)
	// atMost(numero)
	IRepositorioDeLeiloes daoFalso;
	ICarteiro carteiroFalso;

	@Before
	public void criaCampos() {
		daoFalso = mock(IRepositorioDeLeiloes.class);
		carteiroFalso = mock(ICarteiro.class);
	}

	@Test
	public void EncerradorDeLeilaio() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}

	@Test
	public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

		Calendar ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);

		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());

		verify(daoFalso, never()).atualiza(leilao1);// verifica se o metodo nunca foi invocado. pode ser usado o times(0)
		verify(daoFalso, never()).atualiza(leilao2);

	}

	@Test
	public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {
		when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());

	}

	@Test
	public void deveAtualizarLeiloesEncerrados() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

	}

	@Test
	public void deveEnviarEmailAposPersistirLeilaoEncerrado() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);

		encerrador.encerra();

		InOrder inOrder = inOrder(daoFalso, carteiroFalso);
		inOrder.verify(daoFalso, times(1)).atualiza(leilao1);
		inOrder.verify(carteiroFalso, times(1)).envia(leilao1);

	}

	@Test
	public void deveContinuarAExecucaoMesmoQuandoDAOFalha() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
		
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1,leilao2));
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		verify(daoFalso).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
		
		verify(daoFalso, times(1)).atualiza(leilao1);//vai dar OK pq o times verifica quantas vezes o metodo foi invocado
		verify(carteiroFalso, times(0)).envia(leilao1);//vai dar OK pq o ele capota no atualiza que é invocado antes de chegaro ao envia.
	}
	
	  @Test
	    public void deveContinuarAExecucaoMesmoQuandoEnviadorDeEmaillFalha() {
	        Calendar antiga = Calendar.getInstance();
	        antiga.set(1999, 1, 20);

	        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
	            .naData(antiga).constroi();
	        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
	            .naData(antiga).constroi();

	        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
	        doThrow(new RuntimeException()).when(carteiroFalso).envia(leilao1);

	        EncerradorDeLeilao encerrador =
	            new EncerradorDeLeilao(daoFalso, carteiroFalso);

	        encerrador.encerra();

	        verify(daoFalso,times(1)).atualiza(leilao1);
	        verify(carteiroFalso,times(1)).envia(leilao1);//vai dar OK pq o times verifica quantas vezes o metodo foi invocado
	        
	        verify(daoFalso).atualiza(leilao2);
	        verify(carteiroFalso).envia(leilao2);
	    }
	  @Test
	    public void deveDesistirSeDaoFalhaPraSempre() {
	        Calendar antiga = Calendar.getInstance();
	        antiga.set(1999, 1, 20);

	        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
	            .naData(antiga).constroi();
	        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
	            .naData(antiga).constroi();

	        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

	        doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));
	        

	        EncerradorDeLeilao encerrador = 
	            new EncerradorDeLeilao(daoFalso, carteiroFalso);

	        encerrador.encerra();

	        verify(daoFalso).atualiza(leilao1);
	        verify(daoFalso).atualiza(leilao1);
	        verify(carteiroFalso, never()).envia(leilao1);
	        verify(carteiroFalso, never()).envia(leilao2);
	        verify(carteiroFalso, never()).envia(any(Leilao.class));
	    }
}
