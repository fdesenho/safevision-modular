import { Directive, HostListener, ElementRef } from '@angular/core';

@Directive({
  selector: '[appPhoneMask]',
  standalone: true
})
export class PhoneMaskDirective {

  constructor(private el: ElementRef) {}

  @HostListener('input', ['$event'])
  onInputChange(event: any) {
    const input = event.target;
    let value = input.value.replace(/\D/g, ''); // Remove tudo que não é número

    // Limita tamanho (DDI + DDD + 9 dígitos) = 13 dígitos max
    if (value.length > 13) {
      value = value.substring(0, 13);
    }

    // Aplica a formatação: +55 48 99999-9999
    if (value.length === 0) {
      input.value = '';
    } else if (value.length <= 2) {
      input.value = `+${value}`;
    } else if (value.length <= 4) {
      input.value = `+${value.substring(0, 2)} ${value.substring(2)}`;
    } else if (value.length <= 9) {
      input.value = `+${value.substring(0, 2)} ${value.substring(2, 4)} ${value.substring(4)}`;
    } else {
      input.value = `+${value.substring(0, 2)} ${value.substring(2, 4)} ${value.substring(4, 9)}-${value.substring(9)}`;
    }
  }
}
