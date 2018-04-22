package br.com.caelum.leilao.dominio;

import java.util.List;

public interface IRepositorioDeLeiloes {
	
	void salva(Leilao leilao);

	List<Leilao> encerrados();

	List<Leilao> correntes();

	void atualiza(Leilao leilao);
}
