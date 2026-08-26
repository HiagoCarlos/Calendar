import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';

export type SignupField = 'name' | 'email' | 'password' | 'password_confirmation' | 'terms';

/**
 * Group-level validator to ensure password and confirmation match.
 */
function passwordsMatchValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get('password')?.value;
    const confirmation = group.get('password_confirmation')?.value;

    if (!confirmation) {
      return null;
    }

    return password === confirmation ? null : { passwordsMismatch: true };
  };
}

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './signup.html',
  styleUrl: './signup.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Signup {
  private readonly fb = inject(FormBuilder).nonNullable;

  /** Signal tracking form submission attempts. */
  protected readonly submitted = signal(false);

  /** Strongly typed reactive signup form. */
  protected readonly form = this.fb.group(
    {
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      password_confirmation: ['', [Validators.required]],
      terms: [false, [Validators.requiredTrue]],
    },
    { validators: passwordsMatchValidator() },
  );

  /** Helper method to check if a specific error message should be displayed. */
  protected showError(field: SignupField, errorCode: string): boolean {
    const control = this.form.get(field);
    if (!control) {
      return false;
    }

    const shouldEvaluate = control.touched || this.submitted();

    if (errorCode === 'passwordsMismatch') {
      return shouldEvaluate && this.form.hasError('passwordsMismatch') && !!control.value;
    }

    return shouldEvaluate && control.hasError(errorCode);
  }

  protected onSubmit(): void {
    this.submitted.set(true);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // Hook API call here when backend registration endpoint is integrated
  }
}
